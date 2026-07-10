package com.shvarsman.menuplanner.domain.model

data class Recipe(
    val id: Long = 0,
    val title: String,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val photoUri: String? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<StepContentItem> = emptyList()
)