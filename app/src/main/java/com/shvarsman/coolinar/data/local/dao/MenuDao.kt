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
    @Query("SELECT * FROM menu_entries WHERE weekStartDate = :weekStart")
    fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntryWithRecipe>>

    @Transaction
    @Query("SELECT * FROM menu_entries WHERE id = :id")
    suspend fun getByIdWithRecipe(id: Long): MenuEntryWithRecipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MenuEntryEntity): Long

    @Query("DELETE FROM menu_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}