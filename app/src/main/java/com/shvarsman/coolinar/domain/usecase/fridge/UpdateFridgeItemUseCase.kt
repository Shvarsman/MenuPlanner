package com.shvarsman.coolinar.domain.usecase.fridge

import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import javax.inject.Inject

class UpdateFridgeItemUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(item: FridgeItem) {
        require(item.quantity >= 0) { "Количество не может быть отрицательным" }
        repository.updateItem(item)
    }
}