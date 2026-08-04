package com.shvarsman.coolinar.domain.model

data class RecipeIngredient(
    val id: Long = 0,
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double
)