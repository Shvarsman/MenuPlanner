package com.shvarsman.menuplanner.domain.usecase.product

import com.shvarsman.menuplanner.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Long) = repository.deleteProduct(productId)
}