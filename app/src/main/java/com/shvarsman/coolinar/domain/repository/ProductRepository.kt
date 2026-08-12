package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAllProducts(): Flow<List<Product>>
    suspend fun getProduct(id: String): Product?
    suspend fun addProduct(product: Product): String
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: String)
    suspend fun findOrCreate(
        name: String,
        category: Category,
        defaultUnit: MeasureUnit,
        isToTaste: Boolean = false,
        isAlwaysAvailable: Boolean = false
    ): Product
    suspend fun countUsages(productId: String): Int
    suspend fun restoreProduct(id: String)
}