package com.shvarsman.coolinar.domain.usecase.product

import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.repository.ProductRepository
import javax.inject.Inject

class FindOrCreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        name: String, category: Category, defaultUnit: MeasureUnit,
        isToTaste: Boolean = false, isAlwaysAvailable: Boolean = false
    ): Product {
        require(name.isNotBlank()) { "Название продукта не может быть пустым" }
        return repository.findOrCreate(
            name.trim(),
            category,
            defaultUnit,
            isToTaste,
            isAlwaysAvailable
        )
    }
}