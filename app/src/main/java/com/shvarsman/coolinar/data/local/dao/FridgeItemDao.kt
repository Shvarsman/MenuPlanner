package com.shvarsman.coolinar.data.local.dao

import androidx.room.*
import com.shvarsman.coolinar.data.local.entity.FridgeItemEntity
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class FridgeItemWithProduct(
    @Embedded val item: FridgeItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)

@Dao
interface FridgeItemDao {
    @Transaction
    @Query("SELECT * FROM fridge_items WHERE isDeleted = 0")
    fun observeAllWithProduct(): Flow<List<FridgeItemWithProduct>>

    @Transaction
    @Query("SELECT * FROM fridge_items WHERE id = :id AND isDeleted = 0")
    suspend fun getByIdWithProduct(id: String): FridgeItemWithProduct?

    @Query("SELECT * FROM fridge_items WHERE id = :id")
    suspend fun getByIdIncludingDeleted(id: String): FridgeItemEntity?

    @Upsert
    suspend fun insert(item: FridgeItemEntity)

    @Update
    suspend fun update(item: FridgeItemEntity)

    @Query("UPDATE fridge_items SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    @Query("UPDATE fridge_items SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: String, updatedAt: Long)

    @Query("UPDATE fridge_items SET quantity = MAX(0, quantity - :amount), updatedAt = :updatedAt WHERE id = :id")
    suspend fun decreaseQuantity(id: String, amount: Double, updatedAt: Long)

    @Query("SELECT * FROM fridge_items")
    suspend fun getAllIncludingDeleted(): List<FridgeItemEntity>

    @Query("DELETE FROM fridge_items")
    suspend fun deleteAllHard()
}