package com.shvarsman.menuplanner.presentation.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.usecase.product.DeleteProductUseCase
import com.shvarsman.menuplanner.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.buildGroupedRows
import com.shvarsman.menuplanner.presentation.utils.debounceSearch
import com.shvarsman.menuplanner.presentation.utils.mapOnDefault
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
    private val deleteProduct: DeleteProductUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val allProducts: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listState: StateFlow<CatalogListState> = combine(
        allProducts,
        _searchQuery.debounceSearch()
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) }
    }
        .mapOnDefault { filtered ->
            CatalogListState(
                rows = buildGroupedRows(filtered, { it.category }) { it.ordinal },
                isEmpty = filtered.isEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogListState())

    fun delete(product: Product) {
        viewModelScope.launch { runCatching { deleteProduct(product.id) } }
    }
}
