package com.shvarsman.menuplanner.domain.model

/**
 * Ингредиент рецепта. Может быть привязан к продукту из холодильника
 * (fridgeProductId != null) либо быть свободным текстовым ингредиентом.
 */
data class RecipeIngredient(
    val id: Long = 0,
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double
)