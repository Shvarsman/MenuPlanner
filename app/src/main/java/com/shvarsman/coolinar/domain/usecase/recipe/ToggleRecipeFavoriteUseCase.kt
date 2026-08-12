package com.shvarsman.coolinar.domain.usecase.recipe

import com.shvarsman.coolinar.domain.repository.RecipeRepository
import javax.inject.Inject

class ToggleRecipeFavoriteUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: String, isFavorite: Boolean) =
        repository.setFavorite(id, isFavorite)
}