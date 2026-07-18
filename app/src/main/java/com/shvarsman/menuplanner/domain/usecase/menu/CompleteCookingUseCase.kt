package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CompleteCookingUseCase @Inject constructor(
    private val fridgeRepository: FridgeRepository,
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(menuEntryId: Long, recipe: Recipe) {
        val fridgeSnapshot = fridgeRepository.observeItems().first()

        recipe.ingredients.forEach { ingredient ->
            if (ingredient.product.isToTaste) return@forEach // специи и т.п. не списываются поштучно

            var remainingToConsume = ingredient.quantity

            // Все записи холодильника этого продукта, конвертируемые в единицу ингредиента,
            // от меньшей к большей — сначала списываем то, что можно списать целиком
            val candidates = fridgeSnapshot
                .filter { it.product.id == ingredient.product.id }
                .mapNotNull { fridgeItem ->
                    UnitConversion.convert(fridgeItem.quantity, fridgeItem.unit, ingredient.unit)
                        ?.let { fridgeItem to it }
                }
                .sortedBy { it.second }

            for ((fridgeItem, availableInIngredientUnit) in candidates) {
                if (remainingToConsume <= 0.0) break

                if (availableInIngredientUnit <= remainingToConsume) {
                    fridgeRepository.deleteItem(fridgeItem.id)
                    remainingToConsume -= availableInIngredientUnit
                } else {
                    val neededInFridgeUnit =
                        UnitConversion.convert(remainingToConsume, ingredient.unit, fridgeItem.unit)
                            ?: continue
                    fridgeRepository.decreaseQuantity(fridgeItem.id, neededInFridgeUnit)
                    remainingToConsume = 0.0
                }
            }
            // Если remainingToConsume > 0 после цикла — значит, ни одна запись холодильника
            // не сконвертировалась в единицу ингредиента (несовместимые единицы). Как и раньше,
            // такие продукты просто не списываются — это осознанное ограничение, а не баг.
        }

        menuRepository.removeEntry(menuEntryId)
    }
}