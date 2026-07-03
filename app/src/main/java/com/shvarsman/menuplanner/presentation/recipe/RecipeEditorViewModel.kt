package com.shvarsman.menuplanner.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeProductsUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeEditorState(
    val recipeId: Long = 0,
    val title: String = "",
    val photoUri: String? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val saveRecipe: SaveRecipeUseCase,
    getFridgeProducts: GetFridgeProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeEditorState())
    val state: StateFlow<RecipeEditorState> = _state

    val fridgeProducts: StateFlow<List<Product>> = getFridgeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(recipeId: Long) {
        viewModelScope.launch {
            if (recipeId == 0L) {
                _state.value = RecipeEditorState(isLoading = false)
            } else {
                val recipe = recipeRepository.getRecipe(recipeId)
                _state.value = if (recipe != null) {
                    RecipeEditorState(
                        recipeId = recipe.id,
                        title = recipe.title,
                        photoUri = recipe.photoUri,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        isLoading = false
                    )
                } else {
                    RecipeEditorState(isLoading = false)
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value)
    }

    fun onPhotoSelected(uri: String?) {
        _state.value = _state.value.copy(photoUri = uri)
    }

    fun addIngredientFromFridge(product: Product, quantity: Double) {
        val ingredient = RecipeIngredient(
            fridgeProductId = product.id,
            name = product.name,
            unit = product.unit,
            quantity = quantity
        )
        _state.value = _state.value.copy(ingredients = _state.value.ingredients + ingredient)
    }

    fun addCustomIngredient(name: String, unit: MeasureUnit, quantity: Double) {
        val ingredient = RecipeIngredient(name = name, unit = unit, quantity = quantity)
        _state.value = _state.value.copy(ingredients = _state.value.ingredients + ingredient)
    }

    fun removeIngredient(ingredient: RecipeIngredient) {
        _state.value = _state.value.copy(ingredients = _state.value.ingredients - ingredient)
    }

    fun addStep(text: String) {
        if (text.isBlank()) return
        _state.value = _state.value.copy(steps = _state.value.steps + text.trim())
    }

    fun removeStepAt(index: Int) {
        _state.value = _state.value.copy(steps = _state.value.steps.toMutableList().apply { removeAt(index) })
    }

    fun updateStepAt(index: Int, text: String) {
        _state.value = _state.value.copy(
            steps = _state.value.steps.toMutableList().apply { set(index, text) }
        )
    }

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            try {
                saveRecipe(
                    Recipe(
                        id = current.recipeId,
                        title = current.title,
                        photoUri = current.photoUri,
                        ingredients = current.ingredients,
                        steps = current.steps
                    )
                )
                _state.value = current.copy(isSaved = true)
            } catch (e: IllegalArgumentException) {
                _state.value = current.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
