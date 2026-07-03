package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(productId: Long) {
        repository.deleteProduct(productId)
    }
}
