package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.ReservedKey
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.AddToShoppingListUseCase
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import javax.inject.Inject

class AssignRecipeToMenuUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val fridgeRepository: FridgeRepository,
    private val addToShoppingList: AddToShoppingListUseCase,
    private val getReservedQuantities: GetReservedQuantitiesUseCase
) {
    suspend operator fun invoke(day: DayOfWeek, mealType: MealType, recipe: Recipe): Long {
        val reserved = getReservedQuantities()
        val fridgeSnapshot = fridgeRepository.observeItems().first()

        val entryId = menuRepository.addEntry(
            MenuEntry(dayOfWeek = day, mealType = mealType, recipeId = recipe.id)
        )

        recipe.ingredients.forEach { ingredient ->
            if (ingredient.product.isToTaste) return@forEach // специи/соль и т.п. никогда не докупаются автоматически

            // Суммируем ВСЕ записи холодильника этого продукта, а не первую попавшуюся —
            // продукт может быть учтён несколькими записями с разными, но совместимыми единицами.
            val fridgeQty = fridgeSnapshot
                .filter { it.product.id == ingredient.product.id }
                .sumOf { UnitConversion.convert(it.quantity, it.unit, ingredient.unit) ?: 0.0 }

            val canonical = UnitConversion.canonicalUnit(ingredient.unit)
            val reservedAmount = reserved[ReservedKey(ingredient.product.id, canonical)]
            val reservedQty = reservedAmount
                ?.let { UnitConversion.convert(it.amount, it.unit, ingredient.unit) } ?: 0.0

            val trulyAvailable = (fridgeQty - reservedQty).coerceAtLeast(0.0)

            if (trulyAvailable < ingredient.quantity) {
                val shortage = ingredient.quantity - trulyAvailable
                addToShoppingList(ingredient.product, ingredient.unit, shortage)
            }
        }

        return entryId
    }
}