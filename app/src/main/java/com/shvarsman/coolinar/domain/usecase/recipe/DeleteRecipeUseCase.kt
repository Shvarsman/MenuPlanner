package com.shvarsman.coolinar.domain.usecase.recipe

import com.shvarsman.coolinar.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: Long) {
        repository.deleteRecipe(recipeId)
    }
}
