package com.shvarsman.coolinar.presentation.screens.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import com.shvarsman.coolinar.domain.usecase.menu.CompleteCookingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Одно блюдо на экране готовки — рецепт + все записи меню, которые оно закрывает,
 * плюс отметка "готово" (после которой блюдо списано из холодильника и его нельзя откатить). */
data class CookingDishUiState(
    val recipe: Recipe,
    val menuEntryIds: List<String>,
    val isDone: Boolean = false
)

data class CookingState(
    val dishes: List<CookingDishUiState> = emptyList(),
    val isLoading: Boolean = true
) {
    val allDone: Boolean get() = dishes.isNotEmpty() && dishes.all { it.isDone }
}

@HiltViewModel
class CookingViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val completeCookingUseCase: CompleteCookingUseCase,
    private val sessionHolder: CookingSessionHolder
) : ViewModel() {

    private val _state = MutableStateFlow(CookingState())
    val state: StateFlow<CookingState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val pending = sessionHolder.consume()
            val dishes = pending.mapNotNull { dish ->
                val recipe = recipeRepository.getRecipe(dish.recipeId) ?: return@mapNotNull null
                CookingDishUiState(recipe = recipe, menuEntryIds = dish.menuEntryIds)
            }
            _state.value = CookingState(dishes = dishes, isLoading = false)
        }
    }

    /** Отмечает блюдо готовым: списывает ингредиенты из холодильника и удаляет все
     * закрываемые им записи меню — по одному вызову CompleteCookingUseCase на каждую
     * запись, что для повторов одного рецепта корректно списывает ингредиенты дважды. */
    fun markDishDone(recipe: Recipe) {
        val dish =
            _state.value.dishes.firstOrNull { it.recipe.id == recipe.id && !it.isDone } ?: return
        viewModelScope.launch {
            dish.menuEntryIds.forEach { menuEntryId ->
                completeCookingUseCase(menuEntryId, dish.recipe)
            }
            _state.update { current ->
                current.copy(
                    dishes = current.dishes.map {
                        if (it.recipe.id == recipe.id) it.copy(isDone = true) else it
                    }
                )
            }
        }
    }
}