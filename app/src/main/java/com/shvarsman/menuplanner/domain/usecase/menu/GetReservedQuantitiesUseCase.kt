package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Считает, сколько каждого продукта уже "зарезервировано" рецептами,
 * добавленными в меню, но ещё не приготовленными (не списанными из
 * холодильника). Нужно, чтобы при добавлении нового рецепта в меню не
 * посчитать один и тот же остаток холодильника дважды.
 *
 * [excludeMenuEntryId] позволяет исключить конкретную запись меню из подсчёта
 * (используется при проверке готовности самого этого рецепта — его
 * собственная резервация не должна учитываться против него самого).
 */
class GetReservedQuantitiesUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(excludeMenuEntryId: Long? = null): Map<Long, Double> {
        val entries = menuRepository.observeWeekMenu().first()
            .let { entries -> if (excludeMenuEntryId != null) entries.filter { it.id != excludeMenuEntryId } else entries }

        val reserved = mutableMapOf<Long, Double>()
        entries.forEach { entry ->
            val recipe = recipeRepository.getRecipe(entry.recipeId) ?: return@forEach
            recipe.ingredients.forEach { ingredient ->
                reserved[ingredient.product.id] = (reserved[ingredient.product.id] ?: 0.0) + ingredient.quantity
            }
        }
        return reserved
    }
}