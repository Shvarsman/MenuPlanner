package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAllProducts(): Flow<List<Product>>
    suspend fun getProduct(id: Long): Product?
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: Long)

    /** Возвращает существующий продукт по названию (без учёта регистра)
     * либо создаёт новый в каталоге. */
    suspend fun findOrCreate(name: String, category: Category, defaultUnit: MeasureUnit): Product
}