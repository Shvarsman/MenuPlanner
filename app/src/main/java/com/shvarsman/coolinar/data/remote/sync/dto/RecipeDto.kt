package com.shvarsman.coolinar.data.remote.sync.dto

data class RecipeIngredientDto(
    val id: String = "",
    val productId: String = "",
    val unit: String = "",
    val quantity: Double = 0.0
)

data class RecipeDto(
    val title: String = "",
    val category: String = "",
    val photoUri: String? = null,
    val cookingMethod: String? = null,
    val cookingTimeMinutes: Int? = null,
    val difficulty: String = "",
    val description: String = "",
    val stepsSerialized: String = "",
    val stepCount: Int = 0,
    val favorite: Boolean = false,
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)