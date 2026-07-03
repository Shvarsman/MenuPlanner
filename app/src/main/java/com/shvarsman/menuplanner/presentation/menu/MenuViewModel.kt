package com.shvarsman.menuplanner.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.usecase.menu.AddMenuEntryUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.GetWeekMenuUseCase
import com.shvarsman.menuplanner.domain.usecase.menu.RemoveMenuEntryUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    getWeekMenu: GetWeekMenuUseCase,
    getRecipes: GetRecipesUseCase,
    private val addMenuEntry: AddMenuEntryUseCase,
    private val removeMenuEntry: RemoveMenuEntryUseCase
) : ViewModel() {

    val weekMenu: StateFlow<List<MenuEntry>> = getWeekMenu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = getRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pickerTarget = MutableStateFlow<Pair<DayOfWeek, MealType>?>(null)
    val pickerTarget: StateFlow<Pair<DayOfWeek, MealType>?> = _pickerTarget

    fun openRecipePicker(day: DayOfWeek, meal: MealType) {
        _pickerTarget.value = day to meal
    }

    fun closeRecipePicker() {
        _pickerTarget.value = null
    }

    fun assignRecipe(recipe: Recipe) {
        val target = _pickerTarget.value ?: return
        viewModelScope.launch {
            addMenuEntry(MenuEntry(dayOfWeek = target.first, mealType = target.second, recipeId = recipe.id))
            closeRecipePicker()
        }
    }

    fun removeEntry(entry: MenuEntry) {
        viewModelScope.launch { removeMenuEntry(entry.id) }
    }
}
