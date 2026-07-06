package com.shvarsman.menuplanner.domain.usecase.product

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import javax.inject.Inject

class FindOrCreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(name: String, category: Category, defaultUnit: MeasureUnit): Product {
        require(name.isNotBlank()) { "Название продукта не может быть пустым" }
        return repository.findOrCreate(name.trim(), category, defaultUnit)
    }
}