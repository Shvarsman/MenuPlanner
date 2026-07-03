package com.shvarsman.menuplanner.domain.usecase.fridge

import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: FridgeRepository
) {
    suspend operator fun invoke(product: Product) {
        require(product.name.isNotBlank()) { "Название продукта не может быть пустым" }
        require(product.quantity >= 0) { "Количество не может быть отрицательным" }
        repository.updateProduct(product)
    }
}
