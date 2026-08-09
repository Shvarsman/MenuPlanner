package com.shvarsman.coolinar.domain.usecase.fridge

import com.shvarsman.coolinar.domain.repository.FridgeRepository
import javax.inject.Inject

class DeleteFridgeItemUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(itemId: String) = repository.deleteItem(itemId)
}