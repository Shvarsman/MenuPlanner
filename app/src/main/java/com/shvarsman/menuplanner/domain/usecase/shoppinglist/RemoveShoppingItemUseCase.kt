package com.shvarsman.menuplanner.domain.usecase.shoppinglist

import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import javax.inject.Inject

class RemoveShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend operator fun invoke(itemId: Long) = repository.removeItem(itemId)
}
