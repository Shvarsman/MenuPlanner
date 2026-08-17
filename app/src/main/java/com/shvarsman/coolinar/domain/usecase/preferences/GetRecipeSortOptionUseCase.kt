package com.shvarsman.coolinar.domain.usecase.preferences

import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeSortOptionUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<String?> = repository.recipeSortOption
}