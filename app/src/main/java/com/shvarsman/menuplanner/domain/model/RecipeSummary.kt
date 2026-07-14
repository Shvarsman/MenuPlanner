package com.shvarsman.menuplanner.domain.model

/** Lightweight recipe projection for list/grid screens (no ingredients / product joins). */
data class RecipeSummary(
    val id: Long,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val ingredientCount: Int,
    val stepCount: Int
)
