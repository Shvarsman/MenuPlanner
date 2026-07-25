package com.shvarsman.menuplanner.domain.model

data class Recipe(
    val id: Long = 0,
    val title: String,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val photoUri: String? = null,
    val cookingMethod: CookingMethod? = null,
    val cookingTimeMinutes: Int? = null,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<StepContentItem> = emptyList()
)