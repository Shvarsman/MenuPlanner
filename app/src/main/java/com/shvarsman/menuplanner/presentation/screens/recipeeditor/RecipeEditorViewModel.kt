package com.shvarsman.menuplanner.presentation.screens.recipeeditor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.data.local.ImageFileManager
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.StepContentItem
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.menuplanner.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.menuplanner.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.menuplanner.domain.usecase.recipe.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RecipeEditorState(
    val recipeId: Long = 0,
    val title: String = "",
    val category: RecipeCategory = RecipeCategory.OTHER,
    val photoUri: String? = null,
    val cookingMethod: CookingMethod? = null,
    val cookingHours: Int = 0,
    val cookingMinutes: Int = 0,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<StepContentItem> = listOf(StepContentItem.Text("")),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val saveRecipe: SaveRecipeUseCase,
    private val imageFileManager: ImageFileManager,
    private val findOrCreateProduct: FindOrCreateProductUseCase,
    getAllProducts: GetAllProductsUseCase,
    getFridgeItems: GetFridgeItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeEditorState())
    val state: StateFlow<RecipeEditorState> = _state

    private val _focusRequestIndex = MutableStateFlow<Int?>(null)
    val focusRequestIndex: StateFlow<Int?> = _focusRequestIndex

    private val _isIngredientPickerOpen = MutableStateFlow(false)
    val isIngredientPickerOpen: StateFlow<Boolean> = _isIngredientPickerOpen

    val catalog: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var loadedForRecipeId: Long? = null

    fun load(recipeId: Long) {
        if (loadedForRecipeId == recipeId) return

        loadedForRecipeId = recipeId
        viewModelScope.launch {
            if (recipeId == 0L) {
                _state.value = RecipeEditorState(isLoading = false)
                return@launch
            }

            try {
                val recipe = recipeRepository.getRecipe(recipeId)
                _state.value = if (recipe != null) {
                    val steps = recipe.steps.let { list ->
                        if (list.lastOrNull() !is StepContentItem.Text) {
                            list + StepContentItem.Text("")
                        } else list
                    }
                    val totalMinutes = recipe.cookingTimeMinutes ?: 0
                    RecipeEditorState(
                        recipeId = recipe.id,
                        title = recipe.title,
                        category = recipe.category,
                        photoUri = recipe.photoUri,
                        cookingMethod = recipe.cookingMethod,
                        cookingHours = totalMinutes / 60,
                        cookingMinutes = totalMinutes % 60,
                        ingredients = recipe.ingredients,
                        steps = steps,
                        isLoading = false
                    )
                } else {
                    RecipeEditorState(isLoading = false)
                }
            } catch (e: Exception) {
                loadedForRecipeId = null
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Не удалось загрузить рецепт: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onCategoryChange(category: RecipeCategory) {
        _state.update { it.copy(category = category) }
    }

    fun onCookingMethodChange(method: CookingMethod?) {
        _state.update { it.copy(cookingMethod = method) }
    }

    fun onCookingTimeChange(hours: Int, minutes: Int) {
        _state.update { it.copy(cookingHours = hours, cookingMinutes = minutes) }
    }

    fun onCoverPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            val persistedUri = withContext(Dispatchers.IO) {
                imageFileManager.persistImage(uri)
            }
            _state.update { it.copy(photoUri = persistedUri) }
        }
    }

    fun openIngredientPicker() {
        _isIngredientPickerOpen.value = true
    }

    fun closeIngredientPicker() {
        _isIngredientPickerOpen.value = false
    }

    suspend fun createProduct(name: String, category: Category, unit: MeasureUnit): Product =
        findOrCreateProduct(name, category, unit)

    fun addIngredient(product: Product, unit: MeasureUnit, quantity: Double) {
        _state.update { current ->
            current.copy(
                ingredients = current.ingredients + RecipeIngredient(
                    product = product,
                    unit = unit,
                    quantity = quantity
                )
            )
        }
        closeIngredientPicker()
    }

    fun removeIngredient(ingredient: RecipeIngredient) {
        _state.update { it.copy(ingredients = it.ingredients - ingredient) }
    }

    fun onStepTextChange(index: Int, text: String) {
        _state.update { current ->
            current.copy(
                steps = current.steps.mapIndexed { i, item ->
                    if (i == index && item is StepContentItem.Text) item.copy(content = text) else item
                }
            )
        }
    }

    fun onStepNext(currentIndex: Int) {
        val steps = _state.value.steps
        val nextTextIndex = steps.indices
            .drop(currentIndex + 1)
            .firstOrNull { steps[it] is StepContentItem.Text }

        if (nextTextIndex != null) {
            _focusRequestIndex.value = nextTextIndex
        } else {
            addTextStep()
        }
    }

    fun addTextStep() {
        val current = _state.value.steps
        val lastIsEmptyText = current.lastOrNull()
            .let { it is StepContentItem.Text && it.content.isBlank() }

        if (lastIsEmptyText) {
            _focusRequestIndex.value = current.lastIndex
        } else {
            var newLastIndex = -1
            _state.update { state ->
                val newSteps = state.steps + StepContentItem.Text("")
                newLastIndex = newSteps.lastIndex
                state.copy(steps = newSteps)
            }
            _focusRequestIndex.value = newLastIndex
        }
    }

    fun addStepImage(uri: Uri) {
        viewModelScope.launch {
            val persistedUri = withContext(Dispatchers.IO) {
                imageFileManager.persistImage(uri)
            }

            var newLastIndex = -1
            _state.update { state ->
                val current = state.steps.toMutableList()
                if (current.lastOrNull().let {
                        it is StepContentItem.Text && it.content.isBlank()
                    }) {
                    current.removeAt(current.lastIndex)
                }
                current.add(StepContentItem.Image(persistedUri))
                current.add(StepContentItem.Text(""))
                newLastIndex = current.lastIndex
                state.copy(steps = current)
            }
            _focusRequestIndex.value = newLastIndex
        }
    }

    fun deleteStepItem(index: Int) {
        val itemToRemove = _state.value.steps.getOrNull(index)
        if (itemToRemove is StepContentItem.Image) {
            viewModelScope.launch(Dispatchers.IO) {
                imageFileManager.deleteImage(itemToRemove.url)
            }
        }
        _state.update { current ->
            current.copy(
                steps = current.steps.toMutableList()
                    .apply { removeAt(index) }
                    .let { list ->
                        if (list.lastOrNull() !is StepContentItem.Text) {
                            list + StepContentItem.Text("")
                        } else list
                    }
            )
        }
    }

    fun clearFocusRequest() {
        _focusRequestIndex.value = null
    }

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            try {
                require(current.title.isNotBlank()) { "Название рецепта не может быть пустым" }

                val stepsToSave = current.steps.filter {
                    it !is StepContentItem.Text || it.content.isNotBlank()
                }
                require(stepsToSave.isNotEmpty()) { "Добавьте хотя бы один шаг приготовления" }

                val totalMinutes = current.cookingHours * 60 + current.cookingMinutes

                saveRecipe(
                    Recipe(
                        id = current.recipeId,
                        title = current.title,
                        category = current.category,
                        photoUri = current.photoUri,
                        cookingMethod = current.cookingMethod,
                        cookingTimeMinutes = if (totalMinutes > 0) totalMinutes else null,
                        ingredients = current.ingredients,
                        steps = stepsToSave
                    )
                )
                _state.update { it.copy(isSaved = true) }
            } catch (e: IllegalArgumentException) {
                _state.update { it.copy(errorMessage = e.message) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Не удалось сохранить рецепт: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}