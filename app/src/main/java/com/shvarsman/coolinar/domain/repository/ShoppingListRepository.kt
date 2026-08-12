package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun observeItems(): Flow<List<ShoppingListItem>>
    suspend fun addItem(item: ShoppingListItem): String
    suspend fun updateItem(item: ShoppingListItem)
    suspend fun removeItem(id: String)
    suspend fun restoreItem(id: String)
    suspend fun setChecked(id: String, checked: Boolean)
    suspend fun clearChecked()
}