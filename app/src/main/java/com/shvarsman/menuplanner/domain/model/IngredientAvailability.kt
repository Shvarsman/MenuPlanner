package com.shvarsman.menuplanner.domain.model

enum class IngredientAvailability { AVAILABLE, INSUFFICIENT }

/** Зарезервированное количество продукта в каноничной единице измерения. */
data class ReservedAmount(val unit: MeasureUnit, val amount: Double)

/**
 * Ключ резерва: продукт + каноническая единица измерения. Раздельные ключи для
 * несовместимых единиц (например, продукт заказан то в граммах, то в столовых
 * ложках) — иначе один резерв тихо перезатирал бы другой.
 */
data class ReservedKey(val productId: Long, val canonicalUnit: MeasureUnit)

/**
 * Определяет, хватает ли ингредиента в холодильнике для этого рецепта.
 * Суммирует ВСЕ записи холодильника этого продукта, которые можно сконвертировать
 * в единицу ингредиента (одному продукту может соответствовать несколько записей
 * холодильника с разными, но совместимыми единицами — например, "500 г" и "0.3 кг").
 * Записи с несовместимой единицей (холодильник хранит в штуках, рецепт просит
 * граммы) в сумму не попадают — для них конвертация невозможна.
 */
fun RecipeIngredient.availability(
    fridgeItems: List<FridgeItem>,
    reserved: ReservedAmount? = null
): IngredientAvailability {
    // "По вкусу" — количество не имеет значения, всегда считается доступным
    if (product.isToTaste) return IngredientAvailability.AVAILABLE

    val fridgeQtyInIngredientUnit = fridgeItems
        .filter { it.product.id == product.id }
        .sumOf { UnitConversion.convert(it.quantity, it.unit, unit) ?: 0.0 }

    val reservedInIngredientUnit = reserved
        ?.let { UnitConversion.convert(it.amount, it.unit, unit) }
        ?: 0.0

    val trulyAvailable = (fridgeQtyInIngredientUnit - reservedInIngredientUnit).coerceAtLeast(0.0)

    return if (trulyAvailable >= quantity) IngredientAvailability.AVAILABLE
    else IngredientAvailability.INSUFFICIENT
}

/**
 * Считает, сколько каждого продукта уже "зарезервировано" рецептами в меню
 * (кроме [excludeMenuEntryId]), сгруппировано по (продукт, каноническая единица) —
 * см. [ReservedKey]. Раздельные ключи для несовместимых единиц одного продукта
 * не дают одному резерву затереть другой.
 */
fun computeReservedAmounts(
    menuEntries: List<MenuEntry>,
    recipes: List<Recipe>,
    excludeMenuEntryId: Long? = null
): Map<ReservedKey, ReservedAmount> {
    val filteredEntries = if (excludeMenuEntryId != null)
        menuEntries.filter { it.id != excludeMenuEntryId } else menuEntries

    val reserved = mutableMapOf<ReservedKey, ReservedAmount>()
    filteredEntries.forEach { entry ->
        val recipe = recipes.firstOrNull { it.id == entry.recipeId } ?: return@forEach
        recipe.ingredients.forEach { ingredient ->
            val canonical = UnitConversion.canonicalUnit(ingredient.unit)
            val converted = UnitConversion.convert(ingredient.quantity, ingredient.unit, canonical)
                ?: ingredient.quantity
            val key = ReservedKey(ingredient.product.id, canonical)
            val current = reserved[key]
            reserved[key] = ReservedAmount(canonical, (current?.amount ?: 0.0) + converted)
        }
    }
    return reserved
}