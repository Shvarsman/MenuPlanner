package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Переносит все отмеченные ("купленные") позиции списка покупок в холодильник
 * и удаляет их из списка. Если такой же продукт с той же единицей измерения
 * уже есть в холодильнике — количества суммируются, иначе создаётся новая
 * позиция.
 */
class MoveCheckedItemsToFridgeUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val fridgeRepository: FridgeRepository
) {
    suspend operator fun invoke() {
        val checkedItems = shoppingListRepository.observeItems().first().filter { it.isChecked }
        if (checkedItems.isEmpty()) return

        // Группируем на случай нескольких отмеченных позиций одного продукта с одной единицей
        val grouped = checkedItems.groupBy { it.product.id to it.unit }
        val fridgeSnapshot = fridgeRepository.observeItems().first()

        grouped.forEach { (key, itemsGroup) ->
            val (_, unit) = key
            val product = itemsGroup.first().product
            val totalQuantity = itemsGroup.sumOf { it.quantity }

            val existingFridgeItem = fridgeSnapshot.firstOrNull {
                it.product.id == product.id && it.unit == unit
            }

            if (existingFridgeItem != null) {
                fridgeRepository.updateItem(existingFridgeItem.copy(quantity = existingFridgeItem.quantity + totalQuantity))
            } else {
                fridgeRepository.addItem(
                    FridgeItem(
                        product = product,
                        unit = unit,
                        quantity = totalQuantity
                    )
                )
            }
        }

        checkedItems.forEach { shoppingListRepository.removeItem(it.id) }
    }
}