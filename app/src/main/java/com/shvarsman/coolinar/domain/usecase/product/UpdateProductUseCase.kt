package com.shvarsman.coolinar.domain.usecase.product

import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        require(product.name.isNotBlank()) { "Название продукта не может быть пустым" }
        repository.updateProduct(product)
    }
}