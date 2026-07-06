package com.shvarsman.menuplanner.domain.usecase.product

import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAllProducts()
}