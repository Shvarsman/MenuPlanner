package com.shvarsman.coolinar.domain.model

data class Recipe(
    val id: String = "",
    val title: String,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val photoUri: String? = null,
    val cookingMethod: CookingMethod? = null,
    val cookingTimeMinutes: Int? = null,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val description: String = "",
    val isFavorite: Boolean = false,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<StepContentItem> = emptyList()
)