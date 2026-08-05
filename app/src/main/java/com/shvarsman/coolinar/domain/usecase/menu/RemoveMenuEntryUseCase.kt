package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.model.computeReservedAmounts
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import com.shvarsman.coolinar.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Удаляет запись меню и одновременно уменьшает список покупок ровно на ту часть
 * ингредиентов, что нужна была ТОЛЬКО из-за этой записи (маргинальная нехватка):
 * shortageWith - shortageWithout, где shortageWith/Without — нехватка продукта
 * с учётом/без учёта этой записи среди остальных записей той же недели.
 *
 * Продукты "по вкусу" (isToTaste) и "всегда доступные" (isAlwaysAvailable)
 * не трогаем — для них нет количественного учёта нехватки, поэтому нет
 * надёжного способа определить, что убрать из списка покупок.
 */
class RemoveMenuEntryUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository,
    private val fridgeRepository: FridgeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(entryId: Long) {
        transactionRunner.runInTransaction {
            val entry = menuRepository.getEntry(entryId) ?: run {
                menuRepository.removeEntry(entryId)
                return@runInTransaction
            }
            val recipe = recipeRepository.getRecipe(entry.recipeId)

            if (recipe != null && recipe.ingredients.isNotEmpty()) {
                val weekEntries = menuRepository.observeWeekMenu(entry.weekStartDate).first()
                val otherEntries = weekEntries.filter { it.id != entryId }
                val allRecipes = recipeRepository.observeRecipes().first()
                val fridgeSnapshot = fridgeRepository.observeItems().first()

                val demandFromOthers = computeReservedAmounts(otherEntries, allRecipes)
                val shoppingItems = shoppingListRepository.observeItems().first()

                recipe.ingredients.forEach { ingredient ->
                    if (ingredient.product.isAlwaysAvailable || ingredient.product.isToTaste) return@forEach

                    val fridgeQty = fridgeSnapshot
                        .filter { it.product.id == ingredient.product.id }
                        .sumOf { UnitConversion.convert(it.quantity, it.unit, ingredient.unit) ?: 0.0 }

                    val canonical = UnitConversion.canonicalUnit(ingredient.unit)
                    val demandOthersQty = demandFromOthers[com.shvarsman.coolinar.domain.model.ReservedKey(ingredient.product.id, canonical)]
                        ?.let { UnitConversion.convert(it.amount, it.unit, ingredient.unit) } ?: 0.0

                    val shortageWithout = (demandOthersQty - fridgeQty).coerceAtLeast(0.0)
                    val shortageWith = (demandOthersQty + ingredient.quantity - fridgeQty).coerceAtLeast(0.0)
                    val toRemove = shortageWith - shortageWithout

                    if (toRemove > 0.0) {
                        removeFromShoppingList(shoppingItems, ingredient.product.id, ingredient.unit, toRemove)
                    }
                }
            }

            menuRepository.removeEntry(entryId)
        }
    }

    private suspend fun removeFromShoppingList(
        currentItems: List<com.shvarsman.coolinar.domain.model.ShoppingListItem>,
        productId: Long,
        unit: com.shvarsman.coolinar.domain.model.MeasureUnit,
        amount: Double
    ) {
        var remaining = amount
        currentItems
            .filter { it.product.id == productId && !it.isChecked }
            .forEach { item ->
                if (remaining <= 0.0) return@forEach
                val convertedRemaining = UnitConversion.convert(remaining, unit, item.unit) ?: return@forEach
                if (item.quantity <= convertedRemaining) {
                    shoppingListRepository.removeItem(item.id)
                    remaining -= UnitConversion.convert(item.quantity, item.unit, unit) ?: item.quantity
                } else {
                    shoppingListRepository.updateItem(item.copy(quantity = item.quantity - convertedRemaining))
                    remaining = 0.0
                }
            }
    }
}