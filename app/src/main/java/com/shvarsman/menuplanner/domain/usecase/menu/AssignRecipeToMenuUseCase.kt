package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.AddToShoppingListUseCase
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * Добавляет рецепт в меню на выбранный день/приём пищи. Продукты из
 * холодильника не списываются на этом этапе — только на "Готовке".
 *
 * Нехватка считается с учётом того, что часть холодильника уже
 * зарезервирована другими рецептами, ранее добавленными в меню:
 * реально доступно = остаток в холодильнике − то, что уже нужно другим
 * рецептам в меню. Если этого не хватает на текущий рецепт — разница
 * уходит в список покупок.
 */
class AssignRecipeToMenuUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val fridgeRepository: FridgeRepository,
    private val addToShoppingList: AddToShoppingListUseCase,
    private val getReservedQuantities: GetReservedQuantitiesUseCase
) {
    suspend operator fun invoke(day: DayOfWeek, mealType: MealType, recipe: Recipe): Long {
        // Считаем резервы ДО добавления нового рецепта в меню — сам он ещё не должен себя резервировать
        val reserved = getReservedQuantities()
        val fridgeSnapshot = fridgeRepository.observeItems().first()

        val entryId = menuRepository.addEntry(
            MenuEntry(dayOfWeek = day, mealType = mealType, recipeId = recipe.id)
        )

        recipe.ingredients.forEach { ingredient ->
            val fridgeQty = fridgeSnapshot.firstOrNull { it.product.id == ingredient.product.id }?.quantity ?: 0.0
            val alreadyReserved = reserved[ingredient.product.id] ?: 0.0
            val trulyAvailable = (fridgeQty - alreadyReserved).coerceAtLeast(0.0)

            if (trulyAvailable < ingredient.quantity) {
                val shortage = ingredient.quantity - trulyAvailable
                addToShoppingList(ingredient.product, ingredient.unit, shortage)
            }
        }

        return entryId
    }
}