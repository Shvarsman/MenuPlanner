package com.shvarsman.menuplanner.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupFridgeItemDto(
    val productName: String,
    val category: String,
    val unit: String,
    val quantity: Double
)

@Serializable
data class BackupIngredientDto(
    val productName: String,
    val category: String,
    val unit: String,
    val quantity: Double
)

@Serializable
data class BackupStepDto(
    val type: String,
    val text: String? = null,
    val imageFileName: String? = null
)

@Serializable
data class BackupRecipeDto(
    val title: String,
    val category: String,
    val photoFileName: String? = null,
    val cookingMethod: String? = null,
    val cookingTimeMinutes: Int? = null,
    val ingredients: List<BackupIngredientDto>,
    val steps: List<BackupStepDto>
)

@Serializable
data class BackupPayload(
    val version: Int = 2,
    val exportedAt: Long,
    val fridgeItems: List<BackupFridgeItemDto>,
    val recipes: List<BackupRecipeDto>
)