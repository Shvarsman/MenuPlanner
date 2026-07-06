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
 * холодильника на этом этапе НЕ списываются — списание происходит позже,
 * на экране "Готовка" (см. CompleteCookingUseCase).
 *
 * Здесь только вычисляется нехватка ингредиентов относительно текущего
 * холодильника, и недостающее количество добавляется в список покупок.
 */
class AssignRecipeToMenuUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val fridgeRepository: FridgeRepository,
    private val addToShoppingList: AddToShoppingListUseCase
) {
    suspend operator fun invoke(day: DayOfWeek, mealType: MealType, recipe: Recipe): Long {
        val entryId = menuRepository.addEntry(
            MenuEntry(dayOfWeek = day, mealType = mealType, recipeId = recipe.id)
        )

        val fridgeSnapshot = fridgeRepository.observeItems().first()

        recipe.ingredients.forEach { ingredient ->
            val available = fridgeSnapshot.firstOrNull { it.product.id == ingredient.product.id }?.quantity ?: 0.0
            if (available < ingredient.quantity) {
                val shortage = ingredient.quantity - available
                addToShoppingList(ingredient.product, ingredient.unit, shortage)
            }
        }

        return entryId
    }
}