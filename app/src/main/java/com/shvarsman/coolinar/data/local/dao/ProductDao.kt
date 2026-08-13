package com.shvarsman.coolinar.data.local.dao

import androidx.room.*
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getByIdIncludingDeleted(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE LOWER(name) = LOWER(:name) AND isDeleted = 0 LIMIT 1")
    suspend fun findByName(name: String): ProductEntity?

    @Upsert
    suspend fun insert(product: ProductEntity)
    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    @Query("UPDATE products SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: String, updatedAt: Long)

    @Query("SELECT * FROM products")
    suspend fun getAllIncludingDeleted(): List<ProductEntity>

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM recipe_ingredients WHERE productId = :productId) +
            (SELECT COUNT(*) FROM fridge_items WHERE productId = :productId AND isDeleted = 0) +
            (SELECT COUNT(*) FROM shopping_list_items WHERE productId = :productId AND isDeleted = 0)
        """
    )
    suspend fun countUsages(productId: String): Int
}