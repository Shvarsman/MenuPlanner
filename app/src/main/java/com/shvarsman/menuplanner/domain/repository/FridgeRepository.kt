package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun observeProducts(): Flow<List<Product>>
    suspend fun getProduct(id: Long): Product?
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: Long)
    /** Уменьшает количество продукта (например, при списании по рецепту), не уходя в минус. */
    suspend fun decreaseQuantity(id: Long, amount: Double)
}
