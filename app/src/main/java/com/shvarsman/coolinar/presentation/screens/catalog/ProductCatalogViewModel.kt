package com.shvarsman.coolinar.presentation.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.usecase.product.DeleteProductUseCase
import com.shvarsman.coolinar.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.coolinar.domain.usecase.product.ProductInUseException
import com.shvarsman.coolinar.domain.usecase.product.UpdateProductUseCase
import com.shvarsman.coolinar.presentation.utils.GroupedRow
import com.shvarsman.coolinar.presentation.utils.PendingDeleteManager
import com.shvarsman.coolinar.presentation.utils.buildGroupedRows
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

typealias CatalogListRow = GroupedRow<Product, Category>

data class CatalogListState(
    val rows: List<CatalogListRow> = emptyList(),
    val isEmpty: Boolean = true
)

@HiltViewModel
class ProductCatalogViewModel @Inject constructor(
    getAllProducts: GetAllProductsUseCase,
    private val deleteProduct: DeleteProductUseCase,
    private val updateProduct: UpdateProductUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val pendingDeleteManager = PendingDeleteManager<String>(viewModelScope)

    private val _showOnlyCustom = MutableStateFlow(true)
    val showOnlyCustom: StateFlow<Boolean> = _showOnlyCustom
    fun toggleShowOnlyCustom() {
        _showOnlyCustom.value = !_showOnlyCustom.value
    }

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory
    fun selectCategory(category: Category?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private val allProducts: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Продукты после фильтра "Мои/Все" — база для чипов категорий.
    // Не зависит от поискового запроса, чтобы ряд чипов не дёргался при наборе текста.
    private val scopedProducts: StateFlow<List<Product>> = combine(
        allProducts, _showOnlyCustom
    ) { list, onlyCustom ->
        if (onlyCustom) list.filter { !it.isDefault } else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Категории, реально присутствующие в текущей области (Мои/Все) — с числом
     * продуктов в каждой, отсортированы в том же порядке, что и группы списка. */
    val availableCategories: StateFlow<List<Pair<Category, Int>>> = scopedProducts
        .mapOnDefault { list ->
            list.groupingBy { it.category }.eachCount()
                .toList()
                .sortedBy { (category, _) -> category.ordinal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listState: StateFlow<CatalogListState> = combine(
        scopedProducts,
        _searchQuery.debounceSearch(),
        _selectedCategory
    ) { list, query, category ->
        list
            .let { if (category != null) it.filter { p -> p.category == category } else it }
            .let {
                if (query.isBlank()) it else it.filter { p ->
                    p.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
    }
        .mapOnDefault { filtered ->
            CatalogListState(
                rows = buildGroupedRows(filtered, { it.category }) { it.ordinal },
                isEmpty = filtered.isEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogListState())

    private val _pendingForceDelete = MutableStateFlow<Pair<Product, Int>?>(null)
    val pendingForceDelete: StateFlow<Pair<Product, Int>?> = _pendingForceDelete

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct

    fun startEdit(product: Product) {
        _editingProduct.value = product
    }

    fun cancelEdit() {
        _editingProduct.value = null
    }

    fun saveEdit(
        name: String,
        category: Category,
        unit: MeasureUnit,
        isToTaste: Boolean,
        isAlwaysAvailable: Boolean
    ) {
        val current = _editingProduct.value ?: return
        viewModelScope.launch {
            updateProduct(
                current.copy(
                    name = name,
                    category = category,
                    defaultUnit = unit,
                    isToTaste = isToTaste,
                    isAlwaysAvailable = isAlwaysAvailable
                )
            )
            _editingProduct.value = null
        }
    }

    fun requestDelete(product: Product) {
        pendingDeleteManager.requestDelete(product.id) {
            try {
                deleteProduct(product.id)
            } catch (e: ProductInUseException) {
                // Продукт нельзя было удалить сразу — возвращаем его в список
                // и просим явное подтверждение через отдельный диалог
                pendingDeleteManager.undo(product.id)
                _pendingForceDelete.value = product to e.usagesCount
            }
        }
    }

    fun undoDelete(id: String) {
        pendingDeleteManager.undo(id)
    }

    fun confirmForceDelete() {
        val (product, _) = _pendingForceDelete.value ?: return
        viewModelScope.launch {
            deleteProduct(product.id, force = true)
            _pendingForceDelete.value = null
        }
    }

    fun cancelForceDelete() {
        _pendingForceDelete.value = null
    }
}