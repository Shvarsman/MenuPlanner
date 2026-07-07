package com.shvarsman.menuplanner.domain.model

enum class IngredientAvailability { AVAILABLE, INSUFFICIENT }

/**
 * Определяет, хватает ли ингредиента в холодильнике для этого рецепта.
 *
 * [reservedByOthers] — количество этого же продукта, уже "занятое" другими
 * рецептами в меню (не приготовленными). Учитывается, чтобы не считать один
 * и тот же остаток холодильника доступным сразу нескольким рецептам.
 */
fun RecipeIngredient.availability(
    fridgeItems: List<FridgeItem>,
    reservedByOthers: Double = 0.0
): IngredientAvailability {
    val fridgeQty = fridgeItems.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0
    val trulyAvailable = (fridgeQty - reservedByOthers).coerceAtLeast(0.0)

    return if (trulyAvailable >= quantity) {
        IngredientAvailability.AVAILABLE
    } else {
        IngredientAvailability.INSUFFICIENT
    }
}