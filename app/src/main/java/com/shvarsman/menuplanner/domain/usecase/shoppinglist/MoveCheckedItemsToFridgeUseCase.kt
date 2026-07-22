package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoveCheckedItemsToFridgeUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val fridgeRepository: FridgeRepository
) {
    suspend operator fun invoke() {
        val checkedItems = shoppingListRepository.observeItems().first().filter { it.isChecked }
        if (checkedItems.isEmpty()) return

        var fridgeSnapshot = fridgeRepository.observeItems().first()

        checkedItems.forEach { item ->
            val existing = fridgeSnapshot.firstOrNull {
                it.product.id == item.product.id &&
                        UnitConversion.convert(item.quantity, item.unit, it.unit) != null
            }

            if (existing != null) {
                val converted = UnitConversion.convert(item.quantity, item.unit, existing.unit)!!
                val updated = existing.copy(
                    quantity = existing.quantity + converted,
                    expirationDate = item.expirationDate ?: existing.expirationDate
                )
                fridgeRepository.updateItem(updated)
                fridgeSnapshot = fridgeSnapshot.map { if (it.id == updated.id) updated else it }
            } else {
                val newId = fridgeRepository.addItem(
                    FridgeItem(
                        product = item.product, unit = item.unit, quantity = item.quantity,
                        expirationDate = item.expirationDate
                    )
                )
                fridgeSnapshot = fridgeSnapshot + FridgeItem(
                    id = newId, product = item.product, unit = item.unit, quantity = item.quantity,
                    expirationDate = item.expirationDate
                )
            }
        }

        checkedItems.forEach { shoppingListRepository.removeItem(it.id) }
    }
}