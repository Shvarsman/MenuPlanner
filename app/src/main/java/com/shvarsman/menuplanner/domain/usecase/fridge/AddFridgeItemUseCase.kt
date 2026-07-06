package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import javax.inject.Inject

class AddFridgeItemUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(item: FridgeItem): Long {
        require(item.quantity >= 0) { "Количество не может быть отрицательным" }
        return repository.addItem(item)
    }
}