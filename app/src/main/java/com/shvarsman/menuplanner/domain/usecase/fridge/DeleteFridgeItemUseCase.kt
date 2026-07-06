package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import javax.inject.Inject

class DeleteFridgeItemUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(itemId: Long) = repository.deleteItem(itemId)
}