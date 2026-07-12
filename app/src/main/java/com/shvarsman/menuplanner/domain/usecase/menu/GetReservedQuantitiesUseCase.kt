package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.ReservedAmount
import com.shvarsman.menuplanner.domain.model.computeReservedAmounts
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetReservedQuantitiesUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(excludeMenuEntryId: Long? = null): Map<Long, ReservedAmount> {
        val entries = menuRepository.observeWeekMenu().first()
        val recipes = recipeRepository.observeRecipes().first()
        return computeReservedAmounts(entries, recipes, excludeMenuEntryId)
    }
}