package com.shvarsman.menuplanner.data.local.dao

import androidx.room.*
import com.shvarsman.menuplanner.data.local.entity.FridgeItemEntity
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class FridgeItemWithProduct(
    @Embedded val item: FridgeItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)

@Dao
interface FridgeItemDao {
    @Transaction
    @Query("SELECT * FROM fridge_items")
    fun observeAllWithProduct(): Flow<List<FridgeItemWithProduct>>

    @Transaction
    @Query("SELECT * FROM fridge_items WHERE id = :id")
    suspend fun getByIdWithProduct(id: Long): FridgeItemWithProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FridgeItemEntity): Long

    @Update
    suspend fun update(item: FridgeItemEntity)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE fridge_items SET quantity = MAX(0, quantity - :amount) WHERE id = :id")
    suspend fun decreaseQuantity(id: Long, amount: Double)
}