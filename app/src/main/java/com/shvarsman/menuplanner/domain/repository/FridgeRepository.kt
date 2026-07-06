package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.FridgeItem
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {
    fun observeItems(): Flow<List<FridgeItem>>
    suspend fun getItem(id: Long): FridgeItem?
    suspend fun addItem(item: FridgeItem): Long
    suspend fun updateItem(item: FridgeItem)
    suspend fun deleteItem(id: Long)
    suspend fun decreaseQuantity(id: Long, amount: Double)
}
