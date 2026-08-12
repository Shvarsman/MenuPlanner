package com.shvarsman.coolinar.presentation.screens.recipe.list

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.DeleteRecipeUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.GetRecipeSummariesUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.GetRecipesUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.RestoreRecipeUseCase
import com.shvarsman.coolinar.domain.usecase.recipe.ToggleRecipeFavoriteUseCase
import com.shvarsman.coolinar.presentation.utils.debounceSearch
import com.shvarsman.coolinar.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecipeSortOption(@StringRes val displayNameRes: Int) {
    TITLE_ASC(R.string.sort_name_asc),
    TITLE_DESC(R.string.sort_name_desc),
    MOST_INGREDIENTS(R.string.sort_most_ingredients),
    MOST_STEPS(R.string.sort_most_steps),
    TIME_ASC(R.string.sort_time_asc),
    TIME_DESC(R.string.sort_time_desc),
    DIFFICULTY_ASC(R.string.sort_difficulty_asc),
    DIFFICULTY_DESC(R.string.sort_difficulty_desc)
}

enum class RecipeGroupingOption(@StringRes val labelRes: Int) {
    CATEGORY(R.string.group_by_category),
    TIME(R.string.group_by_time),
    DIFFICULTY(R.string.group_by_difficulty),
    COOKING_METHOD(R.string.group_by_cooking_method)
}

enum class RecipeTimeBucket(@StringRes val labelRes: Int) {
    UNDER_15(R.string.time_bucket_under_15),
    MIN_15_30(R.string.time_bucket_15_30),
    MIN_30_60(R.string.time_bucket_30_60),
    OVER_60(R.string.time_bucket_over_60),
    UNKNOWN(R.string.time_bucket_unknown);

    companion object {
        fun of(minutes: Int?): RecipeTimeBucket = when {
            minutes == null -> UNKNOWN
            minutes < 15 -> UNDER_15
            minutes < 30 -> MIN_15_30
            minutes < 60 -> MIN_30_60
            else -> OVER_60
        }
    }
}

/**
 * Заголовок группы, не привязанный к одному enum'у — группировка теперь
 * бывает по категории/времени/сложности/способу приготовления, а иконку
 * (RecipeCategoryIcon) можно нарисовать только для категории, поэтому она
 * хранится отдельным опциональным полем и используется только когда
 * применимо (см. RecipeGroupedList.kt).
 */
data class RecipeGroupHeader(
    val id: String,
    @StringRes val labelRes: Int,
    val category: RecipeCategory? = null
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    getRecipeSummaries: GetRecipeSummariesUseCase,
    getRecipes: GetRecipesUseCase,
    getFridgeItems: GetFridgeItemsUseCase,
    private val deleteRecipe: DeleteRecipeUseCase,
    private val restoreRecipe: RestoreRecipeUseCase,
    private val toggleFavorite: ToggleRecipeFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _selectedCategory = MutableStateFlow<RecipeCategory?>(null)
    val selectedCategory: StateFlow<RecipeCategory?> = _selectedCategory
    fun selectCategory(category: RecipeCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private val _selectedCookingMethod = MutableStateFlow<CookingMethod?>(null)
    val selectedCookingMethod: StateFlow<CookingMethod?> = _selectedCookingMethod
    fun selectCookingMethod(method: CookingMethod?) {
        _selectedCookingMethod.value = if (_selectedCookingMethod.value == method) null else method
    }

    private val _sortOption = MutableStateFlow(RecipeSortOption.TITLE_ASC)
    val sortOption: StateFlow<RecipeSortOption> = _sortOption
    fun selectSortOption(option: RecipeSortOption) {
        _sortOption.value = option
    }

    private val _groupingOption = MutableStateFlow(RecipeGroupingOption.CATEGORY)
    val groupingOption: StateFlow<RecipeGroupingOption> = _groupingOption
    fun selectGroupingOption(option: RecipeGroupingOption) {
        _groupingOption.value = option
    }

    val allRecipes: StateFlow<List<RecipeSummary>> = getRecipeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDelete(id: String) {
        viewModelScope.launch { deleteRecipe(id) }
    }

    fun undoDelete(id: String) {
        viewModelScope.launch { restoreRecipe(id) }
    }

    fun requestDeleteBulk(ids: List<String>) {
        ids.forEach { requestDelete(it) }
    }

    fun undoDeleteBulk(ids: List<String>) {
        ids.forEach { undoDelete(it) }
    }

    fun onToggleFavorite(recipe: RecipeSummary) {
        viewModelScope.launch { toggleFavorite(recipe.id, !recipe.isFavorite) }
    }

    private val fullRecipes = getRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val fridgeItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Рецепты, которые можно приготовить прямо сейчас — у всех ингредиентов
     * (кроме "по вкусу") хватает продукта в холодильнике. Рецепты без единого
     * ингредиента не считаются "доступными" — это, скорее всего, недозаполненный
     * черновик, а не то, что реально стоит предлагать готовить. */
    val suggestedRecipes: StateFlow<List<RecipeSummary>> = combine(
        fullRecipes, fridgeItems, allRecipes
    ) { recipes, fridge, summaries ->
        val availableIds = recipes
            .filter { recipe ->
                recipe.ingredients.isNotEmpty() &&
                        recipe.ingredients.all { it.availability(fridge) == IngredientAvailability.AVAILABLE }
            }
            .map { it.id }
            .toSet()
        summaries.filter { it.id in availableIds }
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Категории с числом рецептов — база для чипа-фильтра, не зависит от
     * поиска/сортировки, чтобы список вариантов не дёргался при наборе текста. */
    val availableCategories: StateFlow<List<Pair<RecipeCategory, Int>>> = allRecipes
        .mapOnDefault { list ->
            list.groupingBy { it.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Способы приготовления с числом рецептов — та же идея, что и для
     * категорий; рецепты без указанного способа в список не попадают. */
    val availableCookingMethods: StateFlow<List<Pair<CookingMethod, Int>>> = allRecipes
        .mapOnDefault { list ->
            list.mapNotNull { it.cookingMethod }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedBy { (method, _) -> method.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Нули (время не указано) всегда уходят в конец списка независимо от
     * направления сортировки — иначе при DESC они оказались бы наверху,
     * что выглядит как "самые долгие рецепты", а не "время неизвестно". */
    private fun timeComparator(descending: Boolean): Comparator<RecipeSummary> {
        val base = compareBy<RecipeSummary> { it.cookingTimeMinutes == null }
        return if (descending) {
            base.thenByDescending { it.cookingTimeMinutes ?: 0 }
        } else {
            base.thenBy { it.cookingTimeMinutes ?: 0 }
        }
    }

    val filteredRecipes: StateFlow<List<RecipeSummary>> = combine(
        allRecipes, _searchQuery.debounceSearch(), _selectedCategory,
        _selectedCookingMethod, _sortOption
    ) { recipes, query, category, method, sort ->
        recipes
            .let { if (category != null) it.filter { r -> r.category == category } else it }
            .let { if (method != null) it.filter { r -> r.cookingMethod == method } else it }
            .let {
                if (query.isBlank()) it else it.filter { r ->
                    r.title.contains(query, ignoreCase = true)
                }
            }
            .let { list ->
                when (sort) {
                    RecipeSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                    RecipeSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
                    RecipeSortOption.MOST_INGREDIENTS -> list.sortedByDescending { it.ingredientCount }
                    RecipeSortOption.MOST_STEPS -> list.sortedByDescending { it.stepCount }
                    RecipeSortOption.TIME_ASC -> list.sortedWith(timeComparator(descending = false))
                    RecipeSortOption.TIME_DESC -> list.sortedWith(timeComparator(descending = true))
                    RecipeSortOption.DIFFICULTY_ASC -> list.sortedBy { it.difficulty.ordinal }
                    RecipeSortOption.DIFFICULTY_DESC -> list.sortedByDescending { it.difficulty.ordinal }
                }
            }
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun group(
        list: List<RecipeSummary>,
        option: RecipeGroupingOption
    ): List<Pair<RecipeGroupHeader, List<RecipeSummary>>> = when (option) {
        RecipeGroupingOption.CATEGORY -> list.groupBy { it.category }
            .toSortedMap(compareBy { it.ordinal })
            .map { (category, items) ->
                RecipeGroupHeader(
                    id = "category_${category.name}",
                    labelRes = category.labelRes,
                    category = category
                ) to items
            }

        RecipeGroupingOption.TIME -> list.groupBy { RecipeTimeBucket.of(it.cookingTimeMinutes) }
            .toSortedMap(compareBy { it.ordinal })
            .map { (bucket, items) ->
                RecipeGroupHeader(id = "time_${bucket.name}", labelRes = bucket.labelRes) to items
            }

        RecipeGroupingOption.DIFFICULTY -> list.groupBy { it.difficulty }
            .toSortedMap(compareBy { it.ordinal })
            .map { (difficulty, items) ->
                RecipeGroupHeader(
                    id = "difficulty_${difficulty.name}",
                    labelRes = difficulty.labelRes
                ) to items
            }

        RecipeGroupingOption.COOKING_METHOD -> list.groupBy { it.cookingMethod }
            .toList()
            .sortedBy { (method, _) -> method?.ordinal ?: Int.MAX_VALUE }
            .map { (method, items) ->
                RecipeGroupHeader(
                    id = "method_${method?.name ?: "none"}",
                    labelRes = method?.labelRes ?: R.string.cooking_method_none
                ) to items
            }
    }

    val groupedRecipes: StateFlow<List<Pair<RecipeGroupHeader, List<RecipeSummary>>>> =
        combine(filteredRecipes, _groupingOption, ::group)
            .mapOnDefault { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipesGrouped: StateFlow<List<Pair<RecipeGroupHeader, List<RecipeSummary>>>> =
        combine(allRecipes, _groupingOption, ::group)
            .mapOnDefault { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestedRecipesGrouped: StateFlow<List<Pair<RecipeGroupHeader, List<RecipeSummary>>>> =
        combine(suggestedRecipes, _groupingOption, ::group)
            .mapOnDefault { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Множественный выбор ──────────────────────────────────────────
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    fun enterSelectionMode(initialId: String) {
        _selectedIds.value = setOf(initialId)
    }

    fun toggleSelection(id: String) {
        _selectedIds.value =
            if (id in _selectedIds.value) _selectedIds.value - id else _selectedIds.value + id
    }

    /** Выбирает все рецепты из ТЕКУЩЕГО отфильтрованного списка, а не вообще
     * все — иначе "выбрать все" при активном фильтре/поиске выбрало бы и то,
     * что не видно на экране, что выглядело бы как баг. */
    fun selectAll() {
        _selectedIds.value = filteredRecipes.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleFavoriteSelected() {
        val items = allRecipes.value.filter { it.id in _selectedIds.value }
        if (items.isEmpty()) return
        val makeFavorite = items.any { !it.isFavorite }
        viewModelScope.launch {
            items.forEach { toggleFavorite(it.id, makeFavorite) }
            _selectedIds.value = emptySet()
        }
    }
}