package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val displayName: Flow<String?>
    suspend fun setDisplayName(name: String)

    val recipeViewMode: Flow<String?>
    suspend fun setRecipeViewMode(mode: String)

    val fridgeSortOption: Flow<String?>
    suspend fun setFridgeSortOption(option: String)

    val fridgeGroupByCategory: Flow<Boolean>
    suspend fun setFridgeGroupByCategory(value: Boolean)

    val shoppingSortOption: Flow<String?>
    suspend fun setShoppingSortOption(option: String)

    val recipeSortOption: Flow<String?>
    suspend fun setRecipeSortOption(option: String)

    val recipeGroupingOption: Flow<String?>
    suspend fun setRecipeGroupingOption(option: String)
}