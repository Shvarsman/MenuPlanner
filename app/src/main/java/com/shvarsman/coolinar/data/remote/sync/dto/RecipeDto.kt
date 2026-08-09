package com.shvarsman.coolinar.data.remote.sync.dto

data class RecipeIngredientDto(
    val id: String = "",
    val productId: String = "",
    val unit: String = "",
    val quantity: Double = 0.0
)

/** stepIndex — позиция StepContentItem.Image в полном списке steps рецепта;
 * используется, чтобы при распаковке подставить новый локальный файл
 * на правильное место, не полагаясь на исходный (device-specific) url. */
data class RecipeStepImageDto(
    val stepIndex: Int = 0,
    val imageBase64: String = ""
)

data class RecipeDto(
    val title: String = "",
    val category: String = "",
    val coverPhotoBase64: String? = null,
    val cookingMethod: String? = null,
    val cookingTimeMinutes: Int? = null,
    val difficulty: String = "",
    val description: String = "",
    val stepsSerialized: String = "",
    val stepCount: Int = 0,
    val stepImages: List<RecipeStepImageDto> = emptyList(),
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)