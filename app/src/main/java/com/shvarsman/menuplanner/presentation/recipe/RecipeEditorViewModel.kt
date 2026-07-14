package com.shvarsman.menuplanner.presentation.recipe

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    // Нужен для покраски ингредиентов в зависимости от наличия в холодильнике
    val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var loadedForRecipeId: Long? = null

    fun load(recipeId: Long) {
        if (loadedForRecipeId == recipeId) return

        viewModelScope.launch {
            loadedForRecipeId = recipeId
            if (recipeId == 0L) {
                _state.value = RecipeEditorState(isLoading = false)
            } else {
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
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value)
    }

    fun onCategoryChange(category: RecipeCategory) {
        _state.value = _state.value.copy(category = category)
    }

    fun onCookingMethodChange(method: CookingMethod?) {
        _state.value = _state.value.copy(cookingMethod = method)
    }

    fun onCookingTimeChange(hours: Int, minutes: Int) {
        _state.value = _state.value.copy(cookingHours = hours, cookingMinutes = minutes)
    }

    fun onCoverPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            val persistedUri = imageFileManager.persistImage(uri)
            _state.value = _state.value.copy(photoUri = persistedUri)
        }
    }

    // ── Ингредиенты ────────────────────────────────────────────────────────────

    fun openIngredientPicker() {
        _isIngredientPickerOpen.value = true
    }

    fun closeIngredientPicker() {
        _isIngredientPickerOpen.value = false
    }

    suspend fun createProduct(name: String, category: Category, unit: MeasureUnit): Product =
        findOrCreateProduct(name, category, unit)

    fun addIngredient(product: Product, unit: MeasureUnit, quantity: Double) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients + RecipeIngredient(
                product = product,
                unit = unit,
                quantity = quantity
            )
        )
        closeIngredientPicker()
    }

    fun removeIngredient(ingredient: RecipeIngredient) {
        _state.value = _state.value.copy(ingredients = _state.value.ingredients - ingredient)
    }

    // ── Шаги ──────────────────────────────────────────────────────────────────

    fun onStepTextChange(index: Int, text: String) {
        _state.value = _state.value.copy(
            steps = _state.value.steps.mapIndexed { i, item ->
                if (i == index && item is StepContentItem.Text) item.copy(content = text) else item
            }
        )
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
            val newSteps = current + StepContentItem.Text("")
            _state.value = _state.value.copy(steps = newSteps)
            _focusRequestIndex.value = newSteps.lastIndex
        }
    }

    fun addStepImage(uri: Uri) {
        viewModelScope.launch {
            val persistedUri = imageFileManager.persistImage(uri)

            val current = _state.value.steps.toMutableList()
            if (current.lastOrNull().let {
                    it is StepContentItem.Text && it.content.isBlank()
                }) {
                current.removeAt(current.lastIndex)
            }
            current.add(StepContentItem.Image(persistedUri))
            current.add(StepContentItem.Text(""))
            _state.value = _state.value.copy(steps = current)
            _focusRequestIndex.value = current.lastIndex
        }
    }

    fun deleteStepItem(index: Int) {
        val itemToRemove = _state.value.steps.getOrNull(index)
        if (itemToRemove is StepContentItem.Image) {
            viewModelScope.launch { imageFileManager.deleteImage(itemToRemove.url) }
        }
        _state.value = _state.value.copy(
            steps = _state.value.steps.toMutableList()
                .apply { removeAt(index) }
                .let { list ->
                    if (list.lastOrNull() !is StepContentItem.Text) {
                        list + StepContentItem.Text("")
                    } else list
                }
        )
    }

    fun clearFocusRequest() {
        _focusRequestIndex.value = null
    }

    // ── Сохранение ─────────────────────────────────────────────────────────────

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            try {
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