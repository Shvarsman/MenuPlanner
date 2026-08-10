package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/**
 * Синхронизирует одну коллекцию users/{uid}/{collectionName} с локальной Room-таблицей.
 * Local — Room-сущность (SyncableEntity), Dto — сериализуемая форма документа Firestore.
 *
 * Room остаётся единственным источником истины для UI: репозиторий сначала пишет
 * в Room (мгновенный отклик, работает офлайн), затем "досылает" в Firestore через
 * push(). Firestore офлайн-очередь сама разрулит реальную отправку при появлении сети.
 */
abstract class FirestoreSyncEngine<Local : SyncableEntity, Dto : Any>(
    private val firestore: FirebaseFirestore,
    private val collectionName: String,
    private val dtoClass: Class<Dto>,
    private val syncScope: SyncScope
) {
    private var listenerRegistration: ListenerRegistration? = null

    // По одному Mutex на entity.id — сериализует push() для одной и той же
    // записи. Без этого два независимых fire-and-forget push (например,
    // сохранение рецепта и последующая фоновая загрузка фото в
    // RecipeRepositoryImpl) могут прийти на сервер в обратном порядке:
    // более ранний push с ещё не загруженным file://-URI подтверждается
    // ПОСЛЕ более позднего push с https://-URL и откатывает фото в UI.
    private val pushMutexes = ConcurrentHashMap<String, Mutex>()

    protected abstract suspend fun Local.toDto(): Dto
    protected abstract fun Dto.toLocal(id: String): Local

    /** Пишет входящие с других устройств/из облака записи в Room. НЕ должно
     * повторно вызывать push() — иначе получится бесконечный цикл записи.
     * Пары (Local, Dto), а не только Local — некоторым сущностям (Recipe)
     * нужны данные из самого Dto (например список ингредиентов), которые
     * не умещаются в одну строку Room-таблицы. */
    protected abstract suspend fun upsertLocal(items: List<Pair<Local, Dto>>)

    /** ВСЕ локальные записи, включая isDeleted=true — нужно для reconcile(). */
    protected abstract suspend fun getAllLocalIncludingDeleted(): List<Local>

    /** Текущая локальная версия записи по id, если есть. Нужна, чтобы отличить
     * актуальный снапшот от запоздавшего (см. startListening). */
    protected abstract suspend fun getLocalById(id: String): Local?

    private fun collection(uid: String) =
        firestore.collection("users").document(uid).collection(collectionName)

    /** Начинает слушать изменения в облаке для этого uid. Вызывать при входе,
     * останавливать (stopListening) при выходе — иначе слушатель утечёт
     * на предыдущего пользователя. */
    fun startListening(uid: String) {
        stopListening()
        listenerRegistration = collection(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            // Это подтверждение НАШЕЙ ЖЕ ещё не отправленной записи — Room уже
            // содержит эти данные (мы писали в Room до push), повторно писать не нужно.
            if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener

            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(dtoClass)?.let { dto -> dto.toLocal(doc.id) to dto }
            }
            if (items.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Отбрасываем запоздавшие снапшоты: если у нас уже есть более
                    // свежая (или такая же по времени) локальная версия записи,
                    // применять пришедшую — значит откатить данные назад.
                    // Именно так пропадали фото рецептов: поздно подтвердившийся
                    // push со старым file://-URI перезаписывал уже применённый
                    // https://-URI из более позднего push.
                    val toApply = items.filter { (incoming, _) ->
                        val existing = getLocalById(incoming.id)
                        existing == null || incoming.updatedAt >= existing.updatedAt
                    }
                    if (toApply.isNotEmpty()) upsertLocal(toApply)
                }
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /** Отправляет одну запись в облако. Вызывается репозиторием сразу после
     * записи в Room. */
    suspend fun push(uid: String, entity: Local) {
        val mutex = pushMutexes.computeIfAbsent(entity.id) { Mutex() }
        mutex.withLock {
            // toDto() тоже внутри лока: для Recipe она читает актуальные
            // ингредиенты из Room на момент реальной отправки, а не на момент
            // постановки в очередь — важно, если конкурирующий push успел
            // что-то поменять, пока этот ждал мьютекс.
            val dto = with(entity) { toDto() }
            syncScope.onPushStarted()
            try {
                // Никакого искусственного таймаута: push() и так вызывается
                // fire-and-forget из SyncScope — ничего в UI это ожидание не блокирует.
                // На реальной мобильной сети (в отличие от почти мгновенной сети
                // эмулятора) документ с фото может доезжать заметно дольше пары
                // секунд — обрубать ожидание раньше времени только вредило: реальная
                // передача продолжалась бы в фоне средствами самого Firestore SDK
                // в любом случае, а мы теряли бы отметку "подтверждено сервером".
                collection(uid).document(entity.id).set(dto).await()
            } catch (e: Exception) {
                android.util.Log.w("FirestoreSync", "push failed for $collectionName/${entity.id}, will retry via Firestore's own offline queue", e)
            } finally {
                syncScope.onPushFinished()
            }
        }
    }

    /**
     * Реконсиляция при входе: "локальное побеждает при конфликте". Каждая
     * локальная запись (включая soft-deleted) безусловно перезаписывает то,
     * что в облаке; записи, которых нет локально вообще (добавлены с другого
     * устройства) — подтягиваются.
     */
    suspend fun reconcile(uid: String) {
        val localAll = getAllLocalIncludingDeleted()
        val localIds = localAll.map { it.id }.toSet()

        localAll.forEach { push(uid, it) }

        val remoteSnapshot = collection(uid).get().await()
        val remoteOnly = remoteSnapshot.documents
            .filter { it.id !in localIds }
            .mapNotNull { doc -> doc.toObject(dtoClass)?.let { dto -> dto.toLocal(doc.id) to dto } }
        if (remoteOnly.isNotEmpty()) {
            upsertLocal(remoteOnly)
        }
    }
}