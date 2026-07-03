package com.shvarsman.menuplanner.domain.usecase.recipe

import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: Long) {
        repository.deleteRecipe(recipeId)
    }
}
