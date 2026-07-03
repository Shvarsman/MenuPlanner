package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun observeItems(): Flow<List<ShoppingListItem>>
    suspend fun addItem(item: ShoppingListItem): Long
    suspend fun updateItem(item: ShoppingListItem)
    suspend fun removeItem(id: Long)
    suspend fun setChecked(id: Long, checked: Boolean)
    suspend fun clearChecked()
}
