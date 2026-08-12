package com.shvarsman.coolinar.data.local.dao

import androidx.room.*
import com.shvarsman.coolinar.data.local.entity.MenuEntryEntity
import com.shvarsman.coolinar.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class MenuEntryWithRecipe(
    @Embedded val entry: MenuEntryEntity,
    @Relation(parentColumn = "recipeId", entityColumn = "id")
    val recipe: RecipeEntity
)

@Dao
interface MenuDao {
    @Transaction
    @Query("SELECT * FROM menu_entries WHERE weekStartDate = :weekStart AND isDeleted = 0")
    fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntryWithRecipe>>

    @Transaction
    @Query("SELECT * FROM menu_entries WHERE id = :id AND isDeleted = 0")
    suspend fun getByIdWithRecipe(id: String): MenuEntryWithRecipe?

    @Query("SELECT * FROM menu_entries WHERE id = :id")
    suspend fun getByIdIncludingDeleted(id: String): MenuEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MenuEntryEntity)

    @Query("UPDATE menu_entries SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    @Query("UPDATE menu_entries SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreById(id: String, updatedAt: Long)
    @Query("SELECT * FROM menu_entries")
    suspend fun getAllIncludingDeleted(): List<MenuEntryEntity>
}