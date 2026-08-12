package com.shvarsman.coolinar.domain.usecase.shoppinglist

import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import javax.inject.Inject

class RestoreShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(itemId: String) = repository.restoreItem(itemId)
}