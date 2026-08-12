package com.shvarsman.coolinar.presentation.screens.recipe.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.usecase.preferences.GetRecipeViewModeUseCase
import com.shvarsman.coolinar.domain.usecase.preferences.SetRecipeViewModeUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.RestoreRecipeUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.ToggleRecipeFavoriteUseCase
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeViewMode
import com.shvarsman.coolinar.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val deleteRecipe: DeleteRecipeUseCase,
    private val restoreRecipe: RestoreRecipeUseCase,
    private val toggleFavorite: ToggleRecipeFavoriteUseCase,
    getRecipeViewMode: GetRecipeViewModeUseCase,
    private val setRecipeViewMode: SetRecipeViewModeUseCase
) : ViewModel() {

    val category: RecipeCategory = RecipeCategory.valueOf(
        savedStateHandle.get<String>("category") ?: RecipeCategory.OTHER.name
    )

    val recipes: StateFlow<List<RecipeSummary>> = getRecipeSummaries()
        .mapOnDefault { list -> list.filter { it.category == category } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDelete(id: String) {
        viewModelScope.launch { deleteRecipe(id) }
    }

    fun undoDelete(id: String) {
        viewModelScope.launch { restoreRecipe(id) }
    }

    fun requestDeleteBulk(ids: List<String>) {
        ids.forEach { requestDelete(it) }
    }

    fun undoDeleteBulk(ids: List<String>) {
        ids.forEach { undoDelete(it) }
    }

    fun onToggleFavorite(recipe: RecipeSummary) {
        viewModelScope.launch { toggleFavorite(recipe.id, !recipe.isFavorite) }
    }

    val viewMode: StateFlow<RecipeViewMode> = getRecipeViewMode()
        .map { stored ->
            stored?.let { runCatching { RecipeViewMode.valueOf(it) }.getOrNull() }
                ?: RecipeViewMode.PHOTO_CARDS
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeViewMode.PHOTO_CARDS)

    fun setViewMode(mode: RecipeViewMode) {
        viewModelScope.launch { setRecipeViewMode(mode.name) }
    }

    // ── Множественный выбор ──────────────────────────────────────────
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    fun enterSelectionMode(initialId: String) {
        _selectedIds.value = setOf(initialId)
    }

    fun toggleSelection(id: String) {
        _selectedIds.value =
            if (id in _selectedIds.value) _selectedIds.value - id else _selectedIds.value + id
    }

    fun selectAll() {
        _selectedIds.value = recipes.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleFavoriteSelected() {
        val items = recipes.value.filter { it.id in _selectedIds.value }
        if (items.isEmpty()) return
        val makeFavorite = items.any { !it.isFavorite }
        viewModelScope.launch {
            items.forEach { toggleFavorite(it.id, makeFavorite) }
            _selectedIds.value = emptySet()
        }
    }
}