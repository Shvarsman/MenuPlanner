package com.shvarsman.menuplanner.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeViewState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RecipeViewViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    getFridgeItems: GetFridgeItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeViewState())
    val state: StateFlow<RecipeViewState> = _state.asStateFlow()

    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(recipeId: Long) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(recipeId)
            _state.value = RecipeViewState(recipe = recipe, isLoading = false)
        }
    }
}