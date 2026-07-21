package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.menuplanner.presentation.utils.debounceSearch
import com.shvarsman.menuplanner.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecipeSortOption(val displayName: String) {
    TITLE_ASC("По названию (А-Я)"),
    TITLE_DESC("По названию (Я-А)"),
    MOST_INGREDIENTS("Больше всего ингредиентов"),
    MOST_STEPS("Больше всего шагов")
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    getRecipeSummaries: GetRecipeSummariesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _selectedCategory = MutableStateFlow<RecipeCategory?>(null)
    val selectedCategory: StateFlow<RecipeCategory?> = _selectedCategory
    fun selectCategory(category: RecipeCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private val _sortOption = MutableStateFlow(RecipeSortOption.TITLE_ASC)
    val sortOption: StateFlow<RecipeSortOption> = _sortOption
    fun selectSortOption(option: RecipeSortOption) {
        _sortOption.value = option
    }

    private val allRecipes: StateFlow<List<RecipeSummary>> = getRecipeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Категории с числом рецептов — база для чипов фильтра, не зависит от поиска
     * и сортировки, чтобы ряд чипов не дёргался при наборе текста. */
    val availableCategories: StateFlow<List<Pair<RecipeCategory, Int>>> = allRecipes
        .mapOnDefault { list ->
            list.groupingBy { it.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<RecipeSummary>> = combine(
        allRecipes, _searchQuery.debounceSearch(), _selectedCategory, _sortOption
    ) { recipes, query, category, sort ->
        recipes
            .let { if (category != null) it.filter { r -> r.category == category } else it }
            .let {
                if (query.isBlank()) it else it.filter { r ->
                    r.title.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
            .let { list ->
                when (sort) {
                    RecipeSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                    RecipeSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
                    RecipeSortOption.MOST_INGREDIENTS -> list.sortedByDescending { it.ingredientCount }
                    RecipeSortOption.MOST_STEPS -> list.sortedByDescending { it.stepCount }
                }
            }
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedRecipes: StateFlow<Map<RecipeCategory, List<RecipeSummary>>> = filteredRecipes
        .mapOnDefault { recipes ->
            recipes.groupBy { it.category }.toSortedMap(compareBy { it.ordinal })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun onDelete(recipe: RecipeSummary) {
        viewModelScope.launch { deleteRecipe(recipe.id) }
    }
}