package com.shvarsman.menuplanner.presentation.screens.recipe.view

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.repository.BackupType
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.usecase.backup.ExportBackupUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.AssignRecipeToMenuUseCase
import com.shvarsman.menuplanner.presentation.utils.PendingDeleteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
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
    private val assignRecipeToMenu: AssignRecipeToMenuUseCase,
    getFridgeItems: GetFridgeItemsUseCase
) : ViewModel() {

    private val _rawRecipe = MutableStateFlow<Recipe?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val pendingDeleteManager = PendingDeleteManager<Long>(viewModelScope)

    val state: StateFlow<RecipeViewState> = combine(
        _rawRecipe, _isLoading, pendingDeleteManager.pendingIds
    ) { recipe, isLoading, pendingIds ->
        RecipeViewState(
            recipe = recipe?.copy(ingredients = recipe.ingredients.filter { it.id !in pendingIds }),
            isLoading = isLoading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeViewState())

    private val _shareState = MutableStateFlow<RecipeShareState>(RecipeShareState.Idle)
    val shareState: StateFlow<RecipeShareState> = _shareState

    private val _isAddToMenuSheetOpen = MutableStateFlow(false)
    val isAddToMenuSheetOpen: StateFlow<Boolean> = _isAddToMenuSheetOpen

    private val _menuAddedEvent =
        MutableStateFlow(0) // счётчик — каждое изменение = новое событие для Snackbar
    val menuAddedEvent: StateFlow<Int> = _menuAddedEvent

    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _rawRecipe.value = recipeRepository.getRecipe(recipeId)
            _isLoading.value = false
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

    fun openAddToMenuSheet() {
        _isAddToMenuSheetOpen.value = true
    }

    fun closeAddToMenuSheet() {
        _isAddToMenuSheetOpen.value = false
    }

    fun confirmAddToMenu(day: DayOfWeek, mealType: MealType) {
        val recipe = _rawRecipe.value ?: return
        viewModelScope.launch {
            assignRecipeToMenu(day, mealType, recipe)
            _isAddToMenuSheetOpen.value = false
            _menuAddedEvent.value += 1
        }
    }

    fun updateIngredient(ingredient: RecipeIngredient, unit: MeasureUnit, quantity: Double) {
        val recipe = _rawRecipe.value ?: return
        val updated = recipe.copy(
            ingredients = recipe.ingredients.map {
                if (it.id == ingredient.id) it.copy(unit = unit, quantity = quantity) else it
            }
        )
        _rawRecipe.value = updated
        viewModelScope.launch { recipeRepository.updateRecipe(updated) }
    }

    fun requestDeleteIngredient(ingredientId: Long) {
        pendingDeleteManager.requestDelete(ingredientId) {
            val recipe = _rawRecipe.value ?: return@requestDelete
            val updated =
                recipe.copy(ingredients = recipe.ingredients.filter { it.id != ingredientId })
            _rawRecipe.value = updated
            recipeRepository.updateRecipe(updated)
        }
    }

    fun undoDeleteIngredient(ingredientId: Long) {
        pendingDeleteManager.undo(ingredientId)
    }
}