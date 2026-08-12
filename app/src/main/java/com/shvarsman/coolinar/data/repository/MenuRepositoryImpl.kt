package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.MenuDao
import com.shvarsman.coolinar.data.local.dao.MenuEntryWithRecipe
import com.shvarsman.coolinar.data.local.entity.MenuEntryEntity
import com.shvarsman.coolinar.data.remote.sync.MenuEntrySyncEngine
import com.shvarsman.coolinar.data.remote.sync.SyncScope
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val dao: MenuDao,
    private val syncEngine: MenuEntrySyncEngine,
    private val authRepository: AuthRepository,
    private val syncScope: SyncScope
) : MenuRepository {

    override fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntry>> =
        dao.observeWeekMenu(weekStart).map { list -> list.map { it.toDomain() } }

    override suspend fun getEntry(id: String): MenuEntry? = dao.getByIdWithRecipe(id)?.toDomain()

    override suspend fun addEntry(entry: MenuEntry): String {
        val id = entry.id.ifBlank { UUID.randomUUID().toString() }
        val entity = MenuEntryEntity(
            id = id,
            weekStartDate = entry.weekStartDate,
            dayOfWeek = entry.dayOfWeek,
            mealType = entry.mealType,
            recipeId = entry.recipeId,
            createdAt = entry.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        dao.insert(entity)
        pushIfSignedIn(entity)
        return id
    }

    override suspend fun removeEntry(id: String) {
        val now = System.currentTimeMillis()
        dao.softDeleteById(id, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    override suspend fun restoreEntry(id: String) {
        val now = System.currentTimeMillis()
        dao.restoreById(id, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    private fun pushIfSignedIn(entity: MenuEntryEntity) {
        val uid = authRepository.currentUserId ?: return
        syncScope.scope.launch {
            runCatching {
                syncEngine.push(uid, entity)
            }
        }
    }
}

private fun MenuEntryWithRecipe.toDomain() = MenuEntry(
    id = entry.id,
    weekStartDate = entry.weekStartDate,
    dayOfWeek = entry.dayOfWeek,
    mealType = entry.mealType,
    recipeId = entry.recipeId,
    recipeTitle = recipe.title,
    recipePhotoUri = recipe.photoUri,
    createdAt = entry.createdAt
)
