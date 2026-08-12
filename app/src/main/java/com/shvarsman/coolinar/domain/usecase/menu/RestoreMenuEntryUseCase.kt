package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import com.shvarsman.coolinar.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Полностью отменяет RemoveMenuEntryUseCase: восстанавливает саму запись
 * меню и реверсирует все побочные изменения списка покупок, зафиксированные
 * в MenuEntryRemovalResult (удалённые позиции — restoreItem, уменьшенные —
 * updateItem с исходным количеством).
 */
class RestoreMenuEntryUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(result: MenuEntryRemovalResult) {
        transactionRunner.runInTransaction {
            menuRepository.restoreEntry(result.entryId)

            result.removedShoppingItemIds.forEach { shoppingListRepository.restoreItem(it) }

            if (result.quantityAdjustedItems.isNotEmpty()) {
                val currentItems = shoppingListRepository.observeItems().first().associateBy { it.id }
                result.quantityAdjustedItems.forEach { adjustment ->
                    currentItems[adjustment.itemId]?.let { item ->
                        shoppingListRepository.updateItem(item.copy(quantity = adjustment.previousQuantity))
                    }
                }
            }
        }
    }
}