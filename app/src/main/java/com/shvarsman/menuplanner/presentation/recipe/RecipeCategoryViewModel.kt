package com.shvarsman.menuplanner.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipesUseCase
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
    getRecipes: GetRecipesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {

    val category: RecipeCategory = RecipeCategory.valueOf(
        savedStateHandle.get<String>("category") ?: RecipeCategory.OTHER.name
    )

    val recipes: StateFlow<List<Recipe>> = getRecipes()
        .map { list -> list.filter { it.category == category } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDelete(recipe: Recipe) {
        viewModelScope.launch { deleteRecipe(recipe.id) }
    }
}