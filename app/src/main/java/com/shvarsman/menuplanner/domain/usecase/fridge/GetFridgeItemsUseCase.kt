package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFridgeItemsUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    operator fun invoke(): Flow<List<FridgeItem>> = repository.observeItems()
}