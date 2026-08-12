package com.shvarsman.coolinar.domain.model

data class RecipeSummary(
    val id: String,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val cookingMethod: CookingMethod?,
    val cookingTimeMinutes: Int?,
    val difficulty: RecipeDifficulty,
    val isFavorite: Boolean,
    val ingredientCount: Int,
    val stepCount: Int
)