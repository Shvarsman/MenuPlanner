package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.FridgeItem
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun observeItems(): Flow<List<FridgeItem>>
    suspend fun getItem(id: String): FridgeItem?
    suspend fun addItem(item: FridgeItem): String
    suspend fun updateItem(item: FridgeItem)
    suspend fun deleteItem(id: String)
    suspend fun restoreItem(id: String)
    suspend fun decreaseQuantity(id: String, amount: Double)
}