package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipes(): Flow<List<Recipe>>
    suspend fun getRecipe(id: Long): Recipe?
    suspend fun addRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: Long)
}
