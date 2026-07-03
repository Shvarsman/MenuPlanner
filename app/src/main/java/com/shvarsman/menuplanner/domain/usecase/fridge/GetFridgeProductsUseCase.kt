package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFridgeProductsUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeProducts()
}
