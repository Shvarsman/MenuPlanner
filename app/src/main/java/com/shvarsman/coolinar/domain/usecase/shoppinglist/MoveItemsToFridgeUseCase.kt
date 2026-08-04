package com.shvarsman.coolinar.domain.usecase.shoppinglist

import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/** Переносит указанные записи списка покупок в холодильник и удаляет их из списка.
 * @param expirationDates срок годности по каждой записи (id -> дата), задаётся
 * пользователем отдельно по каждому товару перед переносом. */
class MoveItemsToFridgeUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val fridgeRepository: FridgeRepository
) {
    suspend operator fun invoke(itemIds: Set<Long>, expirationDates: Map<Long, LocalDate?> = emptyMap()) {
        if (itemIds.isEmpty()) return
        val items = shoppingListRepository.observeItems().first().filter { it.id in itemIds }
        if (items.isEmpty()) return

        var fridgeSnapshot = fridgeRepository.observeItems().first()

        items.forEach { item ->
            val expirationDate = expirationDates[item.id]
            val existing = fridgeSnapshot.firstOrNull {
                it.product.id == item.product.id &&
                        UnitConversion.convert(item.quantity, item.unit, it.unit) != null
            }

            if (existing != null) {
                val converted = UnitConversion.convert(item.quantity, item.unit, existing.unit)!!
                val updated = existing.copy(
                    quantity = existing.quantity + converted,
                    expirationDate = expirationDate ?: existing.expirationDate
                )
                fridgeRepository.updateItem(updated)
                fridgeSnapshot = fridgeSnapshot.map { if (it.id == updated.id) updated else it }
            } else {
                val newId = fridgeRepository.addItem(
                    FridgeItem(
                        product = item.product, unit = item.unit, quantity = item.quantity,
                        expirationDate = expirationDate
                    )
                )
                fridgeSnapshot = fridgeSnapshot + FridgeItem(
                    id = newId, product = item.product, unit = item.unit, quantity = item.quantity,
                    expirationDate = expirationDate
                )
            }
        }

        items.forEach { shoppingListRepository.removeItem(it.id) }
    }
}