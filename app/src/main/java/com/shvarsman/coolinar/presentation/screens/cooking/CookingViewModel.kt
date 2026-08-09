package com.shvarsman.coolinar.presentation.screens.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import com.shvarsman.coolinar.domain.usecase.menu.CompleteCookingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CookingState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false
)

@HiltViewModel
class CookingViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val completeCookingUseCase: CompleteCookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CookingState())
    val state: StateFlow<CookingState> = _state.asStateFlow()

    fun load(recipeId: String) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(recipeId)
            _state.value = CookingState(recipe = recipe, isLoading = false)
        }
    }

    fun finishCooking(menuEntryId: String) {
        val recipe = _state.value.recipe ?: return
        viewModelScope.launch {
            completeCookingUseCase(menuEntryId, recipe)
            _state.value = _state.value.copy(isCompleted = true)
        }
    }
}