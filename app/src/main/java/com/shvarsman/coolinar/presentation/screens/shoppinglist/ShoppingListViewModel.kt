package com.shvarsman.coolinar.presentation.screens.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.coolinar.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.AddToShoppingListUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.GetShoppingListUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.MoveItemsToFridgeUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.RemoveShoppingItemUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.ToggleShoppingItemUseCase
import com.shvarsman.coolinar.domain.usecase.shoppinglist.UpdateShoppingItemUseCase
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
import java.time.LocalDate
import javax.inject.Inject

enum class ShoppingSortOption(@androidx.annotation.StringRes val displayNameRes: Int) {
    NAME_ASC(R.string.sort_name_asc),
    NAME_DESC(R.string.sort_name_desc),
    QUANTITY_ASC(R.string.sort_quantity_asc),
    QUANTITY_DESC(R.string.sort_quantity_desc)
}
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    getShoppingList: GetShoppingListUseCase,
    getAllProducts: GetAllProductsUseCase,
    private val addToShoppingList: AddToShoppingListUseCase,
    private val toggleShoppingItem: ToggleShoppingItemUseCase,
    private val removeShoppingItem: RemoveShoppingItemUseCase,
    private val findOrCreateProduct: FindOrCreateProductUseCase,
    private val moveItemsToFridge: MoveItemsToFridgeUseCase,
    private val updateShoppingItem: UpdateShoppingItemUseCase
) : ViewModel() {

    private val pendingDeleteManager = PendingDeleteManager<String>(viewModelScope)

    private val rawItems: StateFlow<List<ShoppingListItem>> = getShoppingList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<ShoppingListItem>> = combine(
        rawItems, pendingDeleteManager.pendingIds
    ) { list, pendingIds -> list.filter { it.id !in pendingIds } }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDelete(id: String) {
        pendingDeleteManager.requestDelete(id) { removeShoppingItem(id) }
    }

    fun undoDelete(id: String) {
        pendingDeleteManager.undo(id)
    }

    // ── Поиск / фильтр / сортировка ──────────────────────────────────
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

    private val _sortOption = MutableStateFlow(ShoppingSortOption.NAME_ASC)
    val sortOption: StateFlow<ShoppingSortOption> = _sortOption
    fun selectSortOption(option: ShoppingSortOption) {
        _sortOption.value = option
    }

    val availableCategories: StateFlow<List<Pair<Category, Int>>> = items
        .mapOnDefault { list ->
            list.groupingBy { it.product.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val filteredItems: StateFlow<List<ShoppingListItem>> = combine(
        items, _searchQuery.debounceSearch(), _selectedCategory, _sortOption
    ) { list, query, category, sort ->
        list
            .let { if (category != null) it.filter { i -> i.product.category == category } else it }
            .let {
                if (query.isBlank()) it else it.filter { i ->
                    i.product.name.contains(query, ignoreCase = true) ||
                            i.product.nameEn.contains(query, ignoreCase = true)
                }
            }
            .let { filtered ->
                when (sort) {
                    ShoppingSortOption.NAME_ASC -> filtered.sortedBy { it.product.sortName().lowercase() }
                    ShoppingSortOption.NAME_DESC -> filtered.sortedByDescending { it.product.sortName().lowercase() }
                    ShoppingSortOption.QUANTITY_ASC -> filtered.sortedBy { it.quantity }
                    ShoppingSortOption.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity }
                }
            }
    }
        .mapOnDefault { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedUnchecked: StateFlow<Map<Category, List<ShoppingListItem>>> = filteredItems
        .mapOnDefault { list ->
            list.filter { !it.isChecked }
                .groupBy { it.product.category }
                .toSortedMap(compareBy { it.ordinal })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val checkedItems: StateFlow<List<ShoppingListItem>> = filteredItems
        .mapOnDefault { list -> list.filter { it.isChecked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // hasCheckedItems — по ВСЕМ товарам, не только видимым после фильтра/поиска
    val hasCheckedItems: StateFlow<Boolean> = items
        .mapOnDefault { list -> list.any { it.isChecked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val catalog: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPickerOpen = MutableStateFlow(false)
    val isPickerOpen: StateFlow<Boolean> = _isPickerOpen
    fun openPicker() {
        _isPickerOpen.value = true
    }

    fun closePicker() {
        _isPickerOpen.value = false
    }

    suspend fun createProduct(
        name: String, category: Category, unit: MeasureUnit,
        isToTaste: Boolean, isAlwaysAvailable: Boolean
    ): Product = findOrCreateProduct(name, category, unit, isToTaste, isAlwaysAvailable)

    fun addItem(
        product: Product,
        unit: MeasureUnit,
        quantity: Double,
        expirationDate: LocalDate? = null
    ) {
        // expirationDate тут всегда null — диалог добавления в списке покупок
        // сознательно не запрашивает срок годности (см. ProductPickerDialog(showExpirationDate = false))
        viewModelScope.launch {
            addToShoppingList(product, unit, quantity, expirationDate)
            closePicker()
        }
    }

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch { toggleShoppingItem(item.id, !item.isChecked) }
    }

    // ── Редактирование количества по долгому нажатию ─────────────────
    private val _editingItem = MutableStateFlow<ShoppingListItem?>(null)
    val editingItem: StateFlow<ShoppingListItem?> = _editingItem

    fun startEdit(item: ShoppingListItem) {
        _editingItem.value = item
    }

    fun cancelEdit() {
        _editingItem.value = null
    }

    fun confirmEdit(unit: MeasureUnit, quantity: Double) {
        val current = _editingItem.value ?: return
        viewModelScope.launch {
            updateShoppingItem(current.copy(unit = unit, quantity = quantity))
            _editingItem.value = null
        }
    }

    // ── Перенос отмеченного в холодильник — только подтверждение, без даты ──
    private val _showMoveConfirmation = MutableStateFlow(false)
    val showMoveConfirmation: StateFlow<Boolean> = _showMoveConfirmation

    fun requestMoveCheckedToFridge() {
        if (items.value.any { it.isChecked }) _showMoveConfirmation.value = true
    }

    fun cancelMoveToFridge() {
        _showMoveConfirmation.value = false
    }

    fun confirmMoveCheckedToFridge() {
        val ids = items.value.filter { it.isChecked }.map { it.id }.toSet()
        _showMoveConfirmation.value = false
        viewModelScope.launch { moveItemsToFridge(ids) } // без expirationDates — все null, дата задаётся позже в холодильнике
    }
}