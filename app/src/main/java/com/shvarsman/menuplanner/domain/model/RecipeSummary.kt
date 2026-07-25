package com.shvarsman.menuplanner.domain.model

data class RecipeSummary(
    val id: Long,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val difficulty: RecipeDifficulty,
    val ingredientCount: Int,
    val stepCount: Int
)