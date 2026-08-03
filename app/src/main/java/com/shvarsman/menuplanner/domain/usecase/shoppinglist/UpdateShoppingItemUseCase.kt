package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import javax.inject.Inject

class UpdateShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(item: ShoppingListItem) {
        require(item.quantity >= 0) { "Количество не может быть отрицательным" }
        repository.updateItem(item)
    }
}