package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.usecase.fridge.AddFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.DeleteFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.UpdateFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.menuplanner.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.PendingDeleteManager
import com.shvarsman.menuplanner.presentation.utils.buildGroupedRows
import com.shvarsman.menuplanner.presentation.utils.debounceSearch
import com.shvarsman.menuplanner.presentation.utils.mapOnDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

typealias FridgeListRow = GroupedRow<FridgeItem, Category>

enum class FridgeSortOption(val displayName: String) {
    NAME_ASC("По алфавиту (А-Я)"),
    NAME_DESC("По алфавиту (Я-А)"),
    EXPIRATION_SOON("Сначала истекающие"),
    EXPIRATION_LATE("Сначала свежие"),
    QUANTITY_ASC("По количеству (меньше→больше)"),
    QUANTITY_DESC("По количеству (больше→меньше)"),
    FAVORITES_FIRST("Сначала избранное")
}

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
    private val findOrCreateProduct: FindOrCreateProductUseCase
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
    fun toggleGroupByCategory() {
        _groupByCategory.value = !_groupByCategory.value
    }

    fun selectSortOption(option: FridgeSortOption) {
        _sortOption.value = option
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val allItems: StateFlow<List<FridgeItem>> = getFridgeItems()
        .onEach {
            _isLoading.value = false
        } // фикс "продукты не сразу появляются" — раньше isEmpty=true
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        ) // показывал "пусто" до первой эмиссии Room

    val availableCategories: StateFlow<List<Pair<Category, Int>>> = allItems
        .mapOnDefault { list ->
            list.groupingBy { it.product.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val pendingDeleteManager = PendingDeleteManager<Long>(viewModelScope)

    fun requestDelete(id: Long) {
        pendingDeleteManager.requestDelete(id) { deleteFridgeItem(id) }
    }

    fun undoDelete(id: Long) {
        pendingDeleteManager.undo(id)
    }

    fun requestDeleteBulk(ids: List<Long>) {
        ids.forEach { id -> pendingDeleteManager.requestDelete(id) { deleteFridgeItem(id) } }
    }

    fun undoDeleteBulk(ids: List<Long>) {
        ids.forEach { pendingDeleteManager.undo(it) }
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

    val listState: StateFlow<FridgeListState> = combine(
        filterState, pendingDeleteManager.pendingIds
    ) { state, pendingIds ->
        val filtered = state.list
            .filter { it.id !in pendingIds }   // мгновенно скрываем "удаляемые" элементы
            .let { if (state.category != null) it.filter { i -> i.product.category == state.category } else it }
            .let {
                if (state.query.isBlank()) it else it.filter { i ->
                    i.product.name.contains(
                        state.query,
                        ignoreCase = true
                    )
                }
            }

        val sorted = when (state.sort) {
            FridgeSortOption.NAME_ASC -> filtered.sortedBy { it.product.name.lowercase() }
            FridgeSortOption.NAME_DESC -> filtered.sortedByDescending { it.product.name.lowercase() }
            FridgeSortOption.EXPIRATION_SOON -> filtered.sortedWith(compareBy(nullsLast()) { it.expirationDate })
            FridgeSortOption.EXPIRATION_LATE -> filtered.sortedWith(compareByDescending(nullsFirst()) { it.expirationDate })
            FridgeSortOption.QUANTITY_ASC -> filtered.sortedBy { it.quantity }
            FridgeSortOption.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity }
            FridgeSortOption.FAVORITES_FIRST -> filtered.sortedWith(
                compareByDescending<FridgeItem> { it.isFavorite }.thenBy { it.product.name.lowercase() }
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

    // ── Множественный выбор ──────────────────────────────────────────
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun enterSelectionMode(initialId: Long) {
        _selectedIds.value = setOf(initialId)
    }

    fun toggleSelection(id: Long) {
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
            items.any { !it.isFavorite } // если хоть один не в избранном — добавляем все, иначе снимаем у всех
        viewModelScope.launch {
            items.forEach { updateFridgeItem(it.copy(isFavorite = makeFavorite)) }
            _selectedIds.value = emptySet()
        }
    }

    fun toggleFavorite(item: FridgeItem) {
        viewModelScope.launch { updateFridgeItem(item.copy(isFavorite = !item.isFavorite)) }
    }

    suspend fun createProduct(name: String, category: Category, unit: MeasureUnit): Product =
        findOrCreateProduct(name, category, unit)

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

    fun onDeleteClick(item: FridgeItem) {
        viewModelScope.launch { deleteFridgeItem(item.id) }
    }
}