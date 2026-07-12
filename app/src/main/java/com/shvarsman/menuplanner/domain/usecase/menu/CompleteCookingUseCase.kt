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
            val fridgeItem = fridgeSnapshot.firstOrNull { it.product.id == ingredient.product.id }
                ?: return@forEach

            // Переводим нужное количество в единицу измерения, в которой хранится продукт в холодильнике
            val neededInFridgeUnit =
                UnitConversion.convert(ingredient.quantity, ingredient.unit, fridgeItem.unit)
                    ?: return@forEach // единицы несовместимы — пропускаем списание этого ингредиента

            if (fridgeItem.quantity <= neededInFridgeUnit) {
                fridgeRepository.deleteItem(fridgeItem.id)
            } else {
                fridgeRepository.decreaseQuantity(fridgeItem.id, neededInFridgeUnit)
            }
        }

        menuRepository.removeEntry(menuEntryId)
    }
}