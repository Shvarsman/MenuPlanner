package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import javax.inject.Inject

class AddToShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(product: Product, unit: MeasureUnit, quantity: Double) {
        repository.addItem(ShoppingListItem(product = product, unit = unit, quantity = quantity))
    }
}