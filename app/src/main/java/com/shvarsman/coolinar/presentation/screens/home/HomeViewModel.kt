package com.shvarsman.coolinar.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.model.ReservedAmount
import com.shvarsman.coolinar.domain.model.ReservedKey
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.domain.model.computeReservedAmounts
import com.shvarsman.coolinar.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.coolinar.domain.usecase.menu.AssignRecipeToMenuUseCase
import com.shvarsman.coolinar.domain.usecase.menu.GetWeekMenuUseCase
import com.shvarsman.coolinar.domain.usecase.menu.RemoveMenuEntryUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.GetRecipesUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.GetShoppingListUseCase
import com.shvarsman.coolinar.presentation.utils.PendingDeleteManager
import com.shvarsman.coolinar.presentation.utils.debounceSearch
import com.shvarsman.coolinar.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class MenuUiState(
    val weekMenu: List<MenuEntry> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val fridgeItems: List<FridgeItem> = emptyList(),
    val reservedQuantities: Map<ReservedKey, ReservedAmount> = emptyMap(),
    val pickerTarget: Pair<DayOfWeek, MealType>? = null,
    val insufficientDialogEntry: MenuEntry? = null,
    val navigateToCooking: Pair<Long, Long>? = null,
    val recipeSearchQuery: String = "",
    val filteredPickerRecipes: List<Recipe> = emptyList(),
    val selectedDay: DayOfWeek = LocalDate.now().dayOfWeek,
    val suggestedRecipes: List<RecipeSummary> = emptyList(),
    val expiringFridgeItems: List<FridgeItem> = emptyList(),
    val weeklyPlannedCount: Int = 0,
    val weeklyTotalCount: Int = DayOfWeek.entries.size * MealType.entries.size,
    val shoppingListCount: Int = 0
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    getWeekMenu: GetWeekMenuUseCase,
    getRecipes: GetRecipesUseCase,
    getRecipeSummaries: GetRecipeSummariesUseCase,
    getFridgeItems: GetFridgeItemsUseCase,
    getShoppingList: GetShoppingListUseCase,
    private val assignRecipeToMenu: AssignRecipeToMenuUseCase,
    private val removeMenuEntry: RemoveMenuEntryUseCase
) : ViewModel() {

    private val weekMenuFlow = getWeekMenu()
    private val recipesFlow = getRecipes()
    private val fridgeItemsFlow = getFridgeItems()
    private val recipeSummariesFlow = getRecipeSummaries()
    private val shoppingListFlow = getShoppingList()
    private val extrasFlow = combine(
        recipeSummariesFlow,
        shoppingListFlow
    ) { summaries, shopping -> summaries to shopping }

    private val _recipeSearchQuery = MutableStateFlow("")
    private val _pickerTarget = MutableStateFlow<Pair<DayOfWeek, MealType>?>(null)
    private val _insufficientDialogEntry = MutableStateFlow<MenuEntry?>(null)
    private val _navigateToCooking = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _selectedDay = MutableStateFlow<DayOfWeek>(LocalDate.now().dayOfWeek)

    private val pendingDeleteManager = PendingDeleteManager<Long>(viewModelScope)

    private val coreMenuData = combine(
        weekMenuFlow, recipesFlow, fridgeItemsFlow, pendingDeleteManager.pendingIds, extrasFlow
    ) { menu, recipes, fridge, pendingIds, extras ->
        val (summaries, shoppingItems) = extras
        val visibleMenu = menu.filter { it.id !in pendingIds }

        val suggestedIds = recipes
            .filter { r -> r.ingredients.isNotEmpty() && r.ingredients.all { it.availability(fridge) == IngredientAvailability.AVAILABLE } }
            .map { it.id }
            .toSet()
        val suggested = summaries.filter { it.id in suggestedIds }

        val expiringCutoff = LocalDate.now().plusDays(3)
        val expiring = fridge
            .filter { it.expirationDate != null && !it.expirationDate.isAfter(expiringCutoff) }
            .sortedBy { it.expirationDate }

        val filledSlots = visibleMenu.map { it.dayOfWeek to it.mealType }.distinct().size

        CoreMenuData(
            weekMenu = visibleMenu,
            recipes = recipes,
            fridgeItems = fridge,
            reservedQuantities = computeReservedAmounts(visibleMenu, recipes),
            suggestedRecipes = suggested,
            expiringFridgeItems = expiring,
            weeklyPlannedCount = filledSlots,
            shoppingListCount = shoppingItems.size
        )
    }.mapOnDefault { it }

    val uiState: StateFlow<MenuUiState> = combine(
        coreMenuData,
        _pickerTarget,
        _insufficientDialogEntry,
        _navigateToCooking,
        combine(_recipeSearchQuery.debounceSearch(), _selectedDay) { query, day -> query to day }
    ) { core, picker, dialog, nav, queryAndDay ->
        val (query, selectedDay) = queryAndDay
        val filtered = if (query.isBlank()) core.recipes
        else core.recipes.filter { it.title.contains(query, ignoreCase = true) }

        MenuUiState(
            weekMenu = core.weekMenu,
            recipes = core.recipes,
            fridgeItems = core.fridgeItems,
            reservedQuantities = core.reservedQuantities,
            pickerTarget = picker,
            insufficientDialogEntry = dialog,
            navigateToCooking = nav,
            recipeSearchQuery = query,
            filteredPickerRecipes = filtered,
            selectedDay = selectedDay,
            suggestedRecipes = core.suggestedRecipes,
            expiringFridgeItems = core.expiringFridgeItems,
            weeklyPlannedCount = core.weeklyPlannedCount,
            shoppingListCount = core.shoppingListCount
        )
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuUiState())

    fun selectDay(day: DayOfWeek) {
        _selectedDay.value = day
    }

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

    fun requestDeleteEntry(id: Long) {
        pendingDeleteManager.requestDelete(id) { removeMenuEntry(id) }
    }

    fun undoDeleteEntry(id: Long) {
        pendingDeleteManager.undo(id)
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

    private data class CoreMenuData(
        val weekMenu: List<MenuEntry>,
        val recipes: List<Recipe>,
        val fridgeItems: List<FridgeItem>,
        val reservedQuantities: Map<ReservedKey, ReservedAmount>,
        val suggestedRecipes: List<RecipeSummary>,
        val expiringFridgeItems: List<FridgeItem>,
        val weeklyPlannedCount: Int,
        val shoppingListCount: Int
    )
}