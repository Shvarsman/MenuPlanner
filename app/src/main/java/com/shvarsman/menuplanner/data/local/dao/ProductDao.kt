package com.shvarsman.menuplanner.data.local.dao

import androidx.room.*
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Сколько раз продукт используется в рецептах, холодильнике и списке покупок суммарно. */
    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM recipe_ingredients WHERE productId = :productId) +
            (SELECT COUNT(*) FROM fridge_items WHERE productId = :productId) +
            (SELECT COUNT(*) FROM shopping_list_items WHERE productId = :productId)
        """
    )
    suspend fun countUsages(productId: Long): Int
}