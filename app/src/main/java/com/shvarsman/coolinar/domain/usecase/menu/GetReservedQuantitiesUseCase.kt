package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.model.ReservedAmount
import com.shvarsman.coolinar.domain.model.ReservedKey
import com.shvarsman.coolinar.domain.model.computeReservedAmounts
import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetReservedQuantitiesUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(excludeMenuEntryId: Long? = null): Map<ReservedKey, ReservedAmount> {
        val entries = menuRepository.observeWeekMenu().first()
        val recipes = recipeRepository.observeRecipes().first()
        return computeReservedAmounts(entries, recipes, excludeMenuEntryId)
    }
}