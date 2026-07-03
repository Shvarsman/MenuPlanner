package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import javax.inject.Inject

/** Добавляет в список покупок ингредиенты, выбранные из "холодильника". */
class AddProductsToShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(products: List<Product>) {
        products.forEach { product ->
            repository.addItem(
                ShoppingListItem(
                    name = product.name,
                    unit = product.unit,
                    quantity = product.quantity,
                    fridgeProductId = product.id
                )
            )
        }
    }
}
