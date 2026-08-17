package com.shvarsman.coolinar.domain.usecase.preferences

import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetRecipeSortOptionUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(option: String) = repository.setRecipeSortOption(option)
}