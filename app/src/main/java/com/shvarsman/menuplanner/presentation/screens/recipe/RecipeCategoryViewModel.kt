package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.menuplanner.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getRecipeSummaries: GetRecipeSummariesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {

    val category: RecipeCategory = RecipeCategory.valueOf(
        savedStateHandle.get<String>("category") ?: RecipeCategory.OTHER.name
    )

    val recipes: StateFlow<List<RecipeSummary>> = getRecipeSummaries()
        .map { list -> list.filter { it.category == category } }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDelete(recipe: RecipeSummary) {
        viewModelScope.launch { deleteRecipe(recipe.id) }
    }
}
