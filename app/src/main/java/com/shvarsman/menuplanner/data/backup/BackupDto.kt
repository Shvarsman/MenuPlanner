package com.shvarsman.menuplanner.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupProductDto(
    val name: String,
    val category: String,
    val defaultUnit: String
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
    val type: String, // "text" или "image"
    val text: String? = null,
    val imageFileName: String? = null // имя файла внутри архива в папке images/
)

@Serializable
data class BackupRecipeDto(
    val title: String,
    val category: String,
    val photoFileName: String? = null,
    val ingredients: List<BackupIngredientDto>,
    val steps: List<BackupStepDto>
)

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val products: List<BackupProductDto>,
    val recipes: List<BackupRecipeDto>
)