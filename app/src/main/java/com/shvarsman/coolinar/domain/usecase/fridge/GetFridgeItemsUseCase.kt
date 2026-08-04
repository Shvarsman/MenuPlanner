package com.shvarsman.coolinar.domain.usecase.fridge

import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFridgeItemsUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    operator fun invoke(): Flow<List<FridgeItem>> = repository.observeItems()
}