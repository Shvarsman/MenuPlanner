package com.shvarsman.menuplanner.domain.usecase.recipe

import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import javax.inject.Inject

/** Создаёт новый рецепт либо обновляет существующий (если id != 0). */
class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Long {
        require(recipe.title.isNotBlank()) { "Название рецепта не может быть пустым" }
        require(recipe.steps.isNotEmpty()) { "Добавьте хотя бы один шаг приготовления" }
        return if (recipe.id == 0L) {
            repository.addRecipe(recipe)
        } else {
            repository.updateRecipe(recipe)
            recipe.id
        }
    }
}
