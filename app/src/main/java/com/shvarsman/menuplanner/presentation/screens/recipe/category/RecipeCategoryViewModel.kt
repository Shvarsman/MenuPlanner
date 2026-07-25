package com.shvarsman.menuplanner.presentation.screens.recipe.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.menuplanner.presentation.utils.PendingDeleteManager
import com.shvarsman.menuplanner.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val pendingDeleteManager = PendingDeleteManager<Long>(viewModelScope)

    val recipes: StateFlow<List<RecipeSummary>> = combine(
        getRecipeSummaries(), pendingDeleteManager.pendingIds
    ) { list, pendingIds ->
        list.filter { it.category == category && it.id !in pendingIds }
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDelete(id: Long) {
        pendingDeleteManager.requestDelete(id) { deleteRecipe(id) }
    }

    fun undoDelete(id: Long) {
        pendingDeleteManager.undo(id)
    }
}