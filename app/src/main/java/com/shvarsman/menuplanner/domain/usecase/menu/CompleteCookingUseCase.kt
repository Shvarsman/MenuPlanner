package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Завершает готовку: списывает ингредиенты рецепта из холодильника и убирает
 * рецепт из меню. Вызывается на экране "Готовка" по кнопке "Готово".
 *
 * Если в холодильнике продукта меньше или ровно столько, сколько нужно для
 * рецепта — позиция удаляется из холодильника полностью. Если больше —
 * количество уменьшается на нужную величину.
 */
class CompleteCookingUseCase @Inject constructor(
    private val fridgeRepository: FridgeRepository,
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(menuEntryId: Long, recipe: Recipe) {
        val fridgeSnapshot = fridgeRepository.observeItems().first()

        recipe.ingredients.forEach { ingredient ->
            val fridgeItem = fridgeSnapshot.firstOrNull { it.product.id == ingredient.product.id }
                ?: return@forEach

            if (fridgeItem.quantity <= ingredient.quantity) {
                fridgeRepository.deleteItem(fridgeItem.id)
            } else {
                fridgeRepository.decreaseQuantity(fridgeItem.id, ingredient.quantity)
            }
        }

        menuRepository.removeEntry(menuEntryId)
    }
}