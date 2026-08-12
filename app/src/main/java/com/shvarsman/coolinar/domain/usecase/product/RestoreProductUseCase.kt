package com.shvarsman.coolinar.domain.usecase.product

import com.shvarsman.coolinar.domain.repository.ProductRepository
import javax.inject.Inject

class RestoreProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String) = repository.restoreProduct(productId)
}