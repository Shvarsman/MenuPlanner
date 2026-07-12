package com.shvarsman.menuplanner.domain.model

enum class IngredientAvailability { AVAILABLE, INSUFFICIENT }

/** Зарезервированное количество продукта в каноничной единице измерения. */
data class ReservedAmount(val unit: MeasureUnit, val amount: Double)

/**
 * Определяет, хватает ли ингредиента в холодильнике для этого рецепта.
 * Количество в холодильнике конвертируется в единицу измерения ингредиента
 * (граммы <-> килограммы, миллилитры <-> литры), если единицы совместимы.
 * Для несовместимых единиц (например, холодильник хранит в штуках, а рецепт
 * просит граммы) сравнение невозможно — продукт считается недоступным.
 */
fun RecipeIngredient.availability(
    fridgeItems: List<FridgeItem>,
    reserved: ReservedAmount? = null
): IngredientAvailability {
    // "По вкусу" — количество не имеет значения, всегда считается доступным
    if (product.isToTaste) return IngredientAvailability.AVAILABLE

    val fridgeItem = fridgeItems.firstOrNull { it.product.id == product.id }
    val fridgeQtyInIngredientUnit = fridgeItem
        ?.let { UnitConversion.convert(it.quantity, it.unit, unit) }
        ?: 0.0

    val reservedInIngredientUnit = reserved
        ?.let { UnitConversion.convert(it.amount, it.unit, unit) }
        ?: 0.0

    val trulyAvailable = (fridgeQtyInIngredientUnit - reservedInIngredientUnit).coerceAtLeast(0.0)

    return if (trulyAvailable >= quantity) IngredientAvailability.AVAILABLE
    else IngredientAvailability.INSUFFICIENT
}

/**
 * Считает, сколько каждого продукта уже "зарезервировано" рецептами в меню
 * (кроме [excludeMenuEntryId]), в каноничной единице измерения продукта
 * (граммы для массы, миллилитры для объёма).
 */
fun computeReservedAmounts(
    menuEntries: List<MenuEntry>,
    recipes: List<Recipe>,
    excludeMenuEntryId: Long? = null
): Map<Long, ReservedAmount> {
    val filteredEntries = if (excludeMenuEntryId != null)
        menuEntries.filter { it.id != excludeMenuEntryId } else menuEntries

    val reserved = mutableMapOf<Long, ReservedAmount>()
    filteredEntries.forEach { entry ->
        val recipe = recipes.firstOrNull { it.id == entry.recipeId } ?: return@forEach
        recipe.ingredients.forEach { ingredient ->
            val canonical = UnitConversion.canonicalUnit(ingredient.unit)
            val converted = UnitConversion.convert(ingredient.quantity, ingredient.unit, canonical)
                ?: ingredient.quantity
            val current = reserved[ingredient.product.id]
            reserved[ingredient.product.id] = if (current != null && current.unit == canonical) {
                current.copy(amount = current.amount + converted)
            } else {
                ReservedAmount(canonical, converted)
            }
        }
    }
    return reserved
}