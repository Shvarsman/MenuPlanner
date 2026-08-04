package com.shvarsman.coolinar.domain.usecase.shoppinglist

import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    operator fun invoke(): Flow<List<ShoppingListItem>> = repository.observeItems()
}
