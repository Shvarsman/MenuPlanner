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
data class BackupShoppingItemDto(
    val productName: String,
    val category: String,
    val unit: String,
    val quantity: Double,
    val isChecked: Boolean
)

@Serializable
data class BackupMenuEntryDto(
    val dayOfWeek: String,
    val mealType: String,
    val recipeTitle: String // связываем с рецептом по названию, т.к. id рецептов не переносятся между установками
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
    val imageFileName: String? = null,
    val minutes: Int? = null
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

enum class BackupScope { FULL, RECIPES_ONLY, SINGLE_RECIPE }

@Serializable
data class BackupPayload(
    val version: Int = 3,
    val scope: String,
    val exportedAt: Long,
    val fridgeItems: List<BackupFridgeItemDto> = emptyList(),
    val shoppingItems: List<BackupShoppingItemDto> = emptyList(),
    val menuEntries: List<BackupMenuEntryDto> = emptyList(),
    val recipes: List<BackupRecipeDto> = emptyList()
)