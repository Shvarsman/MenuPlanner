package com.shvarsman.coolinar.domain.usecase.product

import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAllProducts()
}