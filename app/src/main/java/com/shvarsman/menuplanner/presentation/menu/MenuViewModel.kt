package com.shvarsman.menuplanner.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.availability
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

@HiltViewModel
class MenuViewModel @Inject constructor(
    getWeekMenu: GetWeekMenuUseCase,
    getRecipes: GetRecipesUseCase,
    getFridgeItems: GetFridgeItemsUseCase,
    private val assignRecipeToMenu: AssignRecipeToMenuUseCase,
    private val removeMenuEntry: RemoveMenuEntryUseCase
) : ViewModel() {

    val weekMenu: StateFlow<List<MenuEntry>> = getWeekMenu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = getRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _recipeSearchQuery = MutableStateFlow("")
    val recipeSearchQuery: StateFlow<String> = _recipeSearchQuery

    /** Сколько каждого продукта уже "занято" рецептами, добавленными в меню
     * (по productId → суммарное нужное количество). Пересчитывается реактивно
     * при изменении меню или списка рецептов. */
    val reservedQuantities: StateFlow<Map<Long, Double>> =
        combine(weekMenu, recipes) { entries, allRecipes ->
            val reserved = mutableMapOf<Long, Double>()
            entries.forEach { entry ->
                val recipe = allRecipes.firstOrNull { it.id == entry.recipeId } ?: return@forEach
                recipe.ingredients.forEach { ingredient ->
                    reserved[ingredient.product.id] = (reserved[ingredient.product.id] ?: 0.0) + ingredient.quantity
                }
            }
            reserved
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _pickerTarget = MutableStateFlow<Pair<DayOfWeek, MealType>?>(null)
    val pickerTarget: StateFlow<Pair<DayOfWeek, MealType>?> = _pickerTarget

    private val _insufficientDialogEntry = MutableStateFlow<MenuEntry?>(null)
    val insufficientDialogEntry: StateFlow<MenuEntry?> = _insufficientDialogEntry

    private val _navigateToCooking = MutableStateFlow<Pair<Long, Long>?>(null)
    val navigateToCooking: StateFlow<Pair<Long, Long>?> = _navigateToCooking

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

    /** Проверяет наличие всех ингредиентов рецепта в холодильнике — только для
     * этого конкретного рецепта, без учёта резервов других рецептов в меню.
     * Резервирование учитывается только при добавлении в меню (чтобы корректно
     * пополнять список покупок), но не при проверке готовности к готовке —
     * здесь важен только физический остаток на данный момент. */
    fun onCookClick(entry: MenuEntry) {
        val recipe = recipes.value.firstOrNull { it.id == entry.recipeId } ?: return

        val allAvailable = recipe.ingredients.all { ingredient ->
            ingredient.availability(fridgeItems.value) == IngredientAvailability.AVAILABLE
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
}