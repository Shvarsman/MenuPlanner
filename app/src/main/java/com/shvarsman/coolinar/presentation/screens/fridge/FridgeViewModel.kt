package com.shvarsman.coolinar.presentation.screens.fridge

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.usecase.fridge.AddFridgeItemUseCase
import com.shvarsman.coolinar.domain.usecase.fridge.DeleteFridgeItemUseCase
import com.shvarsman.coolinar.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.coolinar.domain.usecase.fridge.RestoreFridgeItemUseCase
import com.shvarsman.coolinar.domain.usecase.fridge.UpdateFridgeItemUseCase
import com.shvarsman.coolinar.domain.usecase.preferences.GetFridgeGroupByCategoryUseCase
import com.shvarsman.coolinar.domain.usecase.preferences.GetFridgeSortOptionUseCase
import com.shvarsman.coolinar.domain.usecase.preferences.SetFridgeGroupByCategoryUseCase
import com.shvarsman.coolinar.domain.usecase.preferences.SetFridgeSortOptionUseCase
import com.shvarsman.coolinar.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.coolinar.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.coolinar.presentation.utils.GroupedRow
import com.shvarsman.coolinar.presentation.utils.buildGroupedRows
import com.shvarsman.coolinar.presentation.utils.debounceSearch
import com.shvarsman.coolinar.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

typealias FridgeListRow = GroupedRow<FridgeItem, Category>

enum class FridgeSortOption(@androidx.annotation.StringRes val displayNameRes: Int) {
    NAME_ASC(R.string.sort_name_asc),
    NAME_DESC(R.string.sort_name_desc),
    EXPIRATION_SOON(R.string.sort_expiration_soon),
    EXPIRATION_LATE(R.string.sort_expiration_late),
    QUANTITY_ASC(R.string.sort_quantity_asc),
    QUANTITY_DESC(R.string.sort_quantity_desc),
    FAVORITES_FIRST(R.string.sort_favorites_first)
}

@Immutable
data class FridgeListState(
    val rows: List<FridgeListRow> = emptyList(),
    val isEmpty: Boolean = true
)

@HiltViewModel
class FridgeViewModel @Inject constructor(
    getFridgeItems: GetFridgeItemsUseCase,
    getAllProducts: GetAllProductsUseCase,
    private val addFridgeItem: AddFridgeItemUseCase,
    private val updateFridgeItem: UpdateFridgeItemUseCase,
    private val deleteFridgeItem: DeleteFridgeItemUseCase,
    private val restoreFridgeItem: RestoreFridgeItemUseCase,
    private val findOrCreateProduct: FindOrCreateProductUseCase,
    private val getFridgeSortOption: GetFridgeSortOptionUseCase,
    private val setFridgeSortOption: SetFridgeSortOptionUseCase,
    private val getFridgeGroupByCategory: GetFridgeGroupByCategoryUseCase,
    private val setFridgeGroupByCategory: SetFridgeGroupByCategoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory
    fun selectCategory(category: Category?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private val _sortOption = MutableStateFlow(FridgeSortOption.NAME_ASC)
    val sortOption: StateFlow<FridgeSortOption> = _sortOption

    private val _groupByCategory = MutableStateFlow(false)
    val groupByCategory: StateFlow<Boolean> = _groupByCategory

    init {
        viewModelScope.launch {
            getFridgeSortOption().collect { saved ->
                val option =
                    saved?.let { name -> FridgeSortOption.entries.firstOrNull { it.name == name } }
                if (option != null) _sortOption.value = option
            }
        }
        viewModelScope.launch {
            getFridgeGroupByCategory().collect { saved -> _groupByCategory.value = saved }
        }
    }

    fun toggleGroupByCategory() {
        _groupByCategory.value = !_groupByCategory.value
        viewModelScope.launch { setFridgeGroupByCategory(_groupByCategory.value) }
    }

    fun selectSortOption(option: FridgeSortOption) {
        _sortOption.value = option
        viewModelScope.launch { setFridgeSortOption(option.name) }
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val allItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .onEach {
            _isLoading.value = false
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val availableCategories: StateFlow<List<Pair<Category, Int>>> = allItems
        .mapOnDefault { list ->
            list.groupingBy { it.product.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDelete(id: String) {
        viewModelScope.launch { deleteFridgeItem(id) }
    }

    fun undoDelete(id: String) {
        viewModelScope.launch { restoreFridgeItem(id) }
    }

    fun requestDeleteBulk(ids: List<String>) {
        viewModelScope.launch { ids.forEach { deleteFridgeItem(it) } }
    }

    fun undoDeleteBulk(ids: List<String>) {
        viewModelScope.launch { ids.forEach { restoreFridgeItem(it) } }
    }

    private data class FridgeFilterState(
        val list: List<FridgeItem>,
        val query: String,
        val category: Category?,
        val sort: FridgeSortOption,
        val groupByCategory: Boolean
    )

    private val filterState = combine(
        allItems, _searchQuery.debounceSearch(), _selectedCategory, _sortOption, _groupByCategory
    ) { list, query, category, sort, groupByCategory ->
        FridgeFilterState(list, query, category, sort, groupByCategory)
    }

    val listState: StateFlow<FridgeListState> = filterState.map { state ->
        val filtered = state.list
            .let { if (state.category != null) it.filter { i -> i.product.category == state.category } else it }
            .let {
                if (state.query.isBlank()) it else it.filter { i ->
                    i.product.name.contains(state.query, ignoreCase = true) ||
                            i.product.nameEn.contains(state.query, ignoreCase = true)
                }
            }

        val sorted = when (state.sort) {
            FridgeSortOption.NAME_ASC -> filtered.sortedBy { it.product.sortName().lowercase() }
            FridgeSortOption.NAME_DESC -> filtered.sortedByDescending {
                it.product.sortName().lowercase()
            }

            FridgeSortOption.EXPIRATION_SOON -> filtered.sortedWith(compareBy(nullsLast()) { it.expirationDate })
            FridgeSortOption.EXPIRATION_LATE -> filtered.sortedWith(compareByDescending(nullsFirst()) { it.expirationDate })
            FridgeSortOption.QUANTITY_ASC -> filtered.sortedBy { it.quantity }
            FridgeSortOption.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity }
            FridgeSortOption.FAVORITES_FIRST -> filtered.sortedWith(
                compareByDescending<FridgeItem> { it.isFavorite }.thenBy {
                    it.product.sortName().lowercase()
                }
            )
        }

        val rows = if (state.groupByCategory) {
            buildGroupedRows(sorted, { it.product.category }) { it.ordinal }
        } else {
            sorted.map { GroupedRow.Item(it) }
        }

        FridgeListState(rows = rows, isEmpty = sorted.isEmpty())
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FridgeListState())
    val catalog: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAddPickerOpen = MutableStateFlow(false)
    val isAddPickerOpen: StateFlow<Boolean> = _isAddPickerOpen
    fun openAddPicker() {
        _isAddPickerOpen.value = true
    }

    fun closeAddPicker() {
        _isAddPickerOpen.value = false
    }

    private val _editingItem = MutableStateFlow<FridgeItem?>(null)
    val editingItem: StateFlow<FridgeItem?> = _editingItem
    fun onEditClick(item: FridgeItem) {
        _editingItem.value = item
    }

    fun closeEditDialog() {
        _editingItem.value = null
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    fun clearError() {
        _errorMessage.value = null
    }

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    fun enterSelectionMode(initialId: String) {
        _selectedIds.value = setOf(initialId)
    }

    fun toggleSelection(id: String) {
        _selectedIds.value =
            if (id in _selectedIds.value) _selectedIds.value - id else _selectedIds.value + id
    }

    fun selectAll() {
        _selectedIds.value = allItems.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleFavoriteSelected() {
        val items = allItems.value.filter { it.id in _selectedIds.value }
        if (items.isEmpty()) return
        val makeFavorite =
            items.any { !it.isFavorite }
        viewModelScope.launch {
            items.forEach { updateFridgeItem(it.copy(isFavorite = makeFavorite)) }
            _selectedIds.value = emptySet()
        }
    }

    fun toggleFavorite(item: FridgeItem) {
        viewModelScope.launch { updateFridgeItem(item.copy(isFavorite = !item.isFavorite)) }
    }

    suspend fun createProduct(
        name: String, category: Category, unit: MeasureUnit,
        isToTaste: Boolean, isAlwaysAvailable: Boolean
    ): Product = findOrCreateProduct(name, category, unit, isToTaste, isAlwaysAvailable)

    fun addItem(product: Product, unit: MeasureUnit, quantity: Double, expirationDate: LocalDate?) {
        viewModelScope.launch {
            try {
                val existing = allItems.value.firstOrNull {
                    it.product.id == product.id &&
                            it.expirationDate == expirationDate &&
                            UnitConversion.convert(quantity, unit, it.unit) != null
                }

                if (existing != null) {
                    val converted = UnitConversion.convert(quantity, unit, existing.unit)!!
                    updateFridgeItem(
                        existing.copy(
                            quantity = existing.quantity + converted,
                            expirationDate = expirationDate ?: existing.expirationDate
                        )
                    )
                } else {
                    addFridgeItem(
                        FridgeItem(
                            product = product,
                            unit = unit,
                            quantity = quantity,
                            expirationDate = expirationDate
                        )
                    )
                }
                closeAddPicker()
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateItemQuantity(
        item: FridgeItem,
        unit: MeasureUnit,
        quantity: Double,
        expirationDate: LocalDate?
    ) {
        viewModelScope.launch {
            try {
                updateFridgeItem(
                    item.copy(
                        unit = unit,
                        quantity = quantity,
                        expirationDate = expirationDate
                    )
                )
                closeEditDialog()
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }
}