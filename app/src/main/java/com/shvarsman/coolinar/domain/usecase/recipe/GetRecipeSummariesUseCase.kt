package com.shvarsman.coolinar.domain.usecase.recipe

import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeSummariesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<RecipeSummary>> = repository.observeRecipeSummaries()
}
