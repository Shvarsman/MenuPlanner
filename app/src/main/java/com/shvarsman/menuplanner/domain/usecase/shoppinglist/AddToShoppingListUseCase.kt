package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddToShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(product: Product, unit: MeasureUnit, quantity: Double) {
        val currentItems = repository.observeItems().first()
        val existing = currentItems.firstOrNull { it.product.id == product.id && !it.isChecked }

        if (existing != null) {
            val convertedQuantity = UnitConversion.convert(quantity, unit, existing.unit)
            if (convertedQuantity != null) {
                repository.updateItem(existing.copy(quantity = existing.quantity + convertedQuantity))
                return
            }
        }
        repository.addItem(ShoppingListItem(product = product, unit = unit, quantity = quantity))
    }
}