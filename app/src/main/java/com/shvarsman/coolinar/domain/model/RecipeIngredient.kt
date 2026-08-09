package com.shvarsman.coolinar.domain.model

data class RecipeIngredient(
    val id: String = "",
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double
)