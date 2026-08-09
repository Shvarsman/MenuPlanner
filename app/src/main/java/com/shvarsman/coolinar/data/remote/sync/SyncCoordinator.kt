package com.shvarsman.coolinar.data.remote.sync

import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка входа для запуска синхронизации. Слушает AuthState:
 * SignedOut -> SignedIn — реконсиляция (локальное побеждает) + старт live-слушателей;
 * SignedIn -> SignedOut — остановка слушателей (иначе они продолжат слушать
 * чужой, предыдущий uid).
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val productSync: ProductSyncEngine,
    private val fridgeSync: FridgeItemSyncEngine,
    private val shoppingListSync: ShoppingListSyncEngine,
    private val recipeSync: RecipeSyncEngine,
    private val menuSync: MenuEntrySyncEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val engines = listOf(productSync, fridgeSync, shoppingListSync, recipeSync, menuSync)

    fun start() {
        scope.launch {
            authRepository.authState.distinctUntilChangedBy { it::class }.collect { state ->
                when (state) {
                    is AuthState.SignedIn -> {
                        val uid = state.user.uid
                        engines.forEach { it.reconcile(uid) }
                        engines.forEach { it.startListening(uid) }
                    }
                    AuthState.SignedOut -> {
                        engines.forEach { it.stopListening() }
                    }
                    AuthState.Loading -> Unit
                }
            }
        }
    }
}