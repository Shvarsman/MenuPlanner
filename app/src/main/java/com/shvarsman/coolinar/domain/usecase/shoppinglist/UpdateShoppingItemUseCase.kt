package com.shvarsman.coolinar.domain.usecase.shoppinglist

import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import javax.inject.Inject

class UpdateShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(item: ShoppingListItem) {
        require(item.quantity >= 0) { "Количество не может быть отрицательным" }
        repository.updateItem(item)
    }
}