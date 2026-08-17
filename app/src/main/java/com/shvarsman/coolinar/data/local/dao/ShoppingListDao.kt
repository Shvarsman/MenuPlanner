package com.shvarsman.coolinar.data.local.dao

import androidx.room.*
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import com.shvarsman.coolinar.data.local.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingListItemWithProduct(
    @Embedded val item: ShoppingListItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_list_items WHERE isDeleted = 0")
    fun observeAllWithProduct(): Flow<List<ShoppingListItemWithProduct>>

    @Query("SELECT * FROM shopping_list_items WHERE id = :id")
    suspend fun getByIdIncludingDeleted(id: String): ShoppingListItemEntity?

    @Upsert
    suspend fun insert(item: ShoppingListItemEntity)

    @Update
    suspend fun update(item: ShoppingListItemEntity)

    @Query("UPDATE shopping_list_items SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    @Query("UPDATE shopping_list_items SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: String, updatedAt: Long)

    @Query("UPDATE shopping_list_items SET isChecked = :checked, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean, updatedAt: Long)

    @Query("SELECT id FROM shopping_list_items WHERE isChecked = 1 AND isDeleted = 0")
    suspend fun getCheckedIds(): List<String>

    @Query("UPDATE shopping_list_items SET isDeleted = 1, updatedAt = :updatedAt WHERE isChecked = 1")
    suspend fun softDeleteChecked(updatedAt: Long)

    @Query("SELECT * FROM shopping_list_items")
    suspend fun getAllIncludingDeleted(): List<ShoppingListItemEntity>

    @Query("DELETE FROM shopping_list_items")
    suspend fun deleteAllHard()
}