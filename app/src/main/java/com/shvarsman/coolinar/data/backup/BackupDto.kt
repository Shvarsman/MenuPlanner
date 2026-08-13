package com.shvarsman.coolinar.data.backup

import kotlinx.serialization.Serializable

/** Общее описание продукта внутри бэкапа. Раньше productName/category/unit
 * дублировались отдельно в каждом из трёх DTO (холодильник, список покупок,
 * ингредиенты) — из-за чего в одном месте забыли перенести nameEn/iconKey/
 * isToTaste/isAlwaysAvailable. Теперь один источник для всех трёх мест. */
@Serializable
data class BackupProductRefDto(
    val name: String,
    val category: String,
    val unit: String,
    val isToTaste: Boolean = false,
    val isAlwaysAvailable: Boolean = false
)

@Serializable
data class BackupFridgeItemDto(
    val product: BackupProductRefDto,
    val quantity: Double,
    val expirationDate: String? = null, // LocalDate.toString() — ISO-8601
    val isFavorite: Boolean = false
)

@Serializable
data class BackupShoppingItemDto(
    val product: BackupProductRefDto,
    val quantity: Double,
    val isChecked: Boolean,
    val expirationDate: String? = null
)

@Serializable
data class BackupMenuEntryDto(
    val weekOffset: Int = 0,
    val dayOfWeek: String,
    val mealType: String,
    val recipeTitle: String, // связываем с рецептом по названию, т.к. id рецептов не переносятся между установками
    val createdAt: Long = 0L
)

@Serializable
data class BackupIngredientDto(
    val product: BackupProductRefDto,
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
    val steps: List<BackupStepDto>,
    val difficulty: String = "EASY",
    val description: String = "",
    val isFavorite: Boolean = false
)

enum class BackupScope { FULL, RECIPES_ONLY, SINGLE_RECIPE }

@Serializable
data class BackupPayload(
    val version: Int = 4,
    val scope: String,
    val exportedAt: Long,
    val fridgeItems: List<BackupFridgeItemDto> = emptyList(),
    val shoppingItems: List<BackupShoppingItemDto> = emptyList(),
    val menuEntries: List<BackupMenuEntryDto> = emptyList(),
    val recipes: List<BackupRecipeDto> = emptyList()
)