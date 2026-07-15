package com.shvarsman.menuplanner.presentation.screens.recipe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.repository.BackupType
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.usecase.backup.ExportBackupUseCase
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

sealed interface RecipeShareState {
    data object Idle : RecipeShareState
    data object InProgress : RecipeShareState
    data object Success : RecipeShareState
    data class Error(val message: String) : RecipeShareState
}

@HiltViewModel
class RecipeViewViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val exportBackup: ExportBackupUseCase,
    getFridgeItems: GetFridgeItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeViewState())
    val state: StateFlow<RecipeViewState> = _state.asStateFlow()

    private val _shareState = MutableStateFlow<RecipeShareState>(RecipeShareState.Idle)
    val shareState: StateFlow<RecipeShareState> = _shareState.asStateFlow()

    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(recipeId: Long) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(recipeId)
            _state.value = RecipeViewState(recipe = recipe, isLoading = false)
        }
    }

    fun onShare(recipeId: Long, destinationUri: Uri) {
        viewModelScope.launch {
            _shareState.value = RecipeShareState.InProgress
            try {
                exportBackup(destinationUri, BackupType.SINGLE_RECIPE, recipeId)
                _shareState.value = RecipeShareState.Success
            } catch (e: Exception) {
                _shareState.value =
                    RecipeShareState.Error(e.message ?: "Не удалось сохранить рецепт")
            }
        }
    }

    fun clearShareState() {
        _shareState.value = RecipeShareState.Idle
    }
}