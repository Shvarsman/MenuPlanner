package com.shvarsman.menuplanner.data.local.dao

import androidx.room.*
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import com.shvarsman.menuplanner.data.local.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingListItemWithProduct(
    @Embedded val item: ShoppingListItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_list_items")
    fun observeAllWithProduct(): Flow<List<ShoppingListItemWithProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Update
    suspend fun update(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE shopping_list_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_list_items WHERE isChecked = 1")
    suspend fun clearChecked()
}