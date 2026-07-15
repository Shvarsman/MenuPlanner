package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipeSummariesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    getRecipeSummaries: GetRecipeSummariesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val allRecipes: StateFlow<List<RecipeSummary>> = getRecipeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<RecipeSummary>> = combine(allRecipes, _searchQuery) { recipes, query ->
        if (query.isBlank()) recipes
        else recipes.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDelete(recipe: RecipeSummary) {
        viewModelScope.launch { deleteRecipe(recipe.id) }
    }
}
