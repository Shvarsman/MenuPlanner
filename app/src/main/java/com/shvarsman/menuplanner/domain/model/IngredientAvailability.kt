package com.shvarsman.menuplanner.domain.model

enum class IngredientAvailability { AVAILABLE, INSUFFICIENT }

/**
 * Определяет, хватает ли ингредиента в холодильнике для этого рецепта.
 * Сравнение идёт по product.id; единицы измерения не конвертируются —
 * предполагается, что ингредиент рецепта и позиция в холодильнике используют
 * одинаковую единицу измерения.
 */
fun RecipeIngredient.availability(fridgeItems: List<FridgeItem>): IngredientAvailability {
    val availableQty = fridgeItems
        .firstOrNull { it.product.id == product.id }
        ?.quantity ?: 0.0

    return if (availableQty >= quantity) {
        IngredientAvailability.AVAILABLE
    } else {
        IngredientAvailability.INSUFFICIENT
    }
}