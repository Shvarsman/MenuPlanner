package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Добавляет продукт в список покупок. Если такой же продукт с такой же
 * единицей измерения уже есть в списке и ещё не куплен — суммирует количество
 * вместо создания дубликата.
 */
class AddToShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(product: Product, unit: MeasureUnit, quantity: Double) {
        val currentItems = repository.observeItems().first()

        val existing = currentItems.firstOrNull {
            it.product.id == product.id && it.unit == unit && !it.isChecked
        }

        if (existing != null) {
            repository.updateItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            repository.addItem(ShoppingListItem(product = product, unit = unit, quantity = quantity))
        }
    }
}