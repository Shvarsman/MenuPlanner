package com.shvarsman.coolinar.domain.usecase.recipe

import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> = repository.observeRecipes()
}
