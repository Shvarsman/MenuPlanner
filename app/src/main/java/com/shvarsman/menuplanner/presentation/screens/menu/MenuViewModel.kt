package com.shvarsman.menuplanner.presentation.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.ReservedAmount
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.domain.model.computeReservedAmounts
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.AssignRecipeToMenuUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.GetWeekMenuUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.RemoveMenuEntryUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

data class MenuUiState(
    val weekMenu: List<MenuEntry> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val fridgeItems: List<FridgeItem> = emptyList(),
    val reservedQuantities: Map<Long, ReservedAmount> = emptyMap(),
    val pickerTarget: Pair<DayOfWeek, MealType>? = null,
    val insufficientDialogEntry: MenuEntry? = null,
    val navigateToCooking: Pair<Long, Long>? = null,
    val recipeSearchQuery: String = ""
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    getWeekMenu: GetWeekMenuUseCase,
    getRecipes: GetRecipesUseCase,
    getFridgeItems: GetFridgeItemsUseCase,
    private val assignRecipeToMenu: AssignRecipeToMenuUseCase,
    private val removeMenuEntry: RemoveMenuEntryUseCase
) : ViewModel() {

    private val weekMenuFlow = getWeekMenu()
    private val recipesFlow = getRecipes()
    private val fridgeItemsFlow = getFridgeItems()

    private val _recipeSearchQuery = MutableStateFlow("")
    private val _pickerTarget = MutableStateFlow<Pair<DayOfWeek, MealType>?>(null)
    private val _insufficientDialogEntry = MutableStateFlow<MenuEntry?>(null)
    private val _navigateToCooking = MutableStateFlow<Pair<Long, Long>?>(null)

    private val dialogState = combine(
        _pickerTarget,
        _insufficientDialogEntry,
        _navigateToCooking,
        _recipeSearchQuery
    ) { picker, dialog, nav, query ->
        DialogSlice(picker, dialog, nav, query)
    }

    val uiState: StateFlow<MenuUiState> = combine(
        weekMenuFlow,
        recipesFlow,
        fridgeItemsFlow,
        dialogState
    ) { menu, recipes, fridge, dialog ->
        MenuUiState(
            weekMenu = menu,
            recipes = recipes,
            fridgeItems = fridge,
            reservedQuantities = computeReservedAmounts(menu, recipes),
            pickerTarget = dialog.pickerTarget,
            insufficientDialogEntry = dialog.insufficientDialogEntry,
            navigateToCooking = dialog.navigateToCooking,
            recipeSearchQuery = dialog.recipeSearchQuery
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuUiState())

    fun openRecipePicker(day: DayOfWeek, meal: MealType) {
        _pickerTarget.value = day to meal
    }

    fun closeRecipePicker() {
        _pickerTarget.value = null
        _recipeSearchQuery.value = ""
    }

    fun assignRecipe(recipe: Recipe) {
        val target = _pickerTarget.value ?: return
        viewModelScope.launch {
            assignRecipeToMenu(target.first, target.second, recipe)
            closeRecipePicker()
        }
    }

    fun removeEntry(entry: MenuEntry) {
        viewModelScope.launch { removeMenuEntry(entry.id) }
    }

    fun onCookClick(entry: MenuEntry) {
        val recipe = uiState.value.recipes.firstOrNull { it.id == entry.recipeId } ?: return

        val allAvailable = recipe.ingredients.all { ingredient ->
            ingredient.availability(uiState.value.fridgeItems) == IngredientAvailability.AVAILABLE
        }

        if (allAvailable) {
            _navigateToCooking.value = recipe.id to entry.id
        } else {
            _insufficientDialogEntry.value = entry
        }
    }

    fun confirmCookAnyway() {
        val entry = _insufficientDialogEntry.value ?: return
        _navigateToCooking.value = entry.recipeId to entry.id
        _insufficientDialogEntry.value = null
    }

    fun dismissInsufficientDialog() {
        _insufficientDialogEntry.value = null
    }

    fun onNavigateToCookingConsumed() {
        _navigateToCooking.value = null
    }

    fun onRecipeSearchQueryChange(query: String) {
        _recipeSearchQuery.value = query
    }

    private data class DialogSlice(
        val pickerTarget: Pair<DayOfWeek, MealType>?,
        val insufficientDialogEntry: MenuEntry?,
        val navigateToCooking: Pair<Long, Long>?,
        val recipeSearchQuery: String
    )
}
