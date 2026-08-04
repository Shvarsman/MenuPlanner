package com.shvarsman.coolinar.domain.usecase.shoppinglist

import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import javax.inject.Inject

class ToggleShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(itemId: Long, checked: Boolean) = repository.setChecked(itemId, checked)
}
