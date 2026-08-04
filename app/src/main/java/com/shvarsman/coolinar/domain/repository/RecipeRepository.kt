package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipeSummaries(): Flow<List<RecipeSummary>>
    fun observeRecipes(): Flow<List<Recipe>>
    suspend fun getRecipe(id: Long): Recipe?
    suspend fun addRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: Long)
}
