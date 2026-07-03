package com.shvarsman.menuplanner.data.local.dao

import androidx.room.*
import com.shvarsman.menuplanner.data.local.entity.MenuEntryEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

data class MenuEntryWithRecipe(
    @Embedded val entry: MenuEntryEntity,
    @Relation(parentColumn = "recipeId", entityColumn = "id")
    val recipe: RecipeEntity
)

@Dao
interface MenuDao {
    @Transaction
    @Query("SELECT * FROM menu_entries")
    fun observeWeekMenu(): Flow<List<MenuEntryWithRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MenuEntryEntity): Long

    @Query("DELETE FROM menu_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
