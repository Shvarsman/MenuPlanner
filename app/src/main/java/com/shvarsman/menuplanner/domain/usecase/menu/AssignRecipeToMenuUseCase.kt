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
 * Добавляет рецепт в меню на выбранный день/приём пищи и одновременно
 * списывает ингредиенты рецепта из холодильника:
 *
 * - если в холодильнике продукта хватает — вычитаем нужное количество;
 * - если продукта нет вообще — всё нужное количество уходит в список покупок;
 * - если продукта не хватает — остаток в холодильнике удаляется полностью,
 *   а недостающее количество (needed - available) добавляется в список покупок.
 *
 * Добавление в список покупок идёт через AddToShoppingListUseCase, который
 * сам суммирует количество с уже существующей непроверенной позицией того же
 * продукта — так дубликаты не создаются.
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
            val needed = ingredient.quantity
            val fridgeItem = fridgeSnapshot.firstOrNull { it.product.id == ingredient.product.id }

            when {
                fridgeItem == null -> {
                    addToShoppingList(ingredient.product, ingredient.unit, needed)
                }

                fridgeItem.quantity >= needed -> {
                    fridgeRepository.decreaseQuantity(fridgeItem.id, needed)
                }

                else -> {
                    val shortage = needed - fridgeItem.quantity
                    fridgeRepository.deleteItem(fridgeItem.id)
                    addToShoppingList(ingredient.product, ingredient.unit, shortage)
                }
            }
        }

        return entryId
    }
}