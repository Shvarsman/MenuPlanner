package com.shvarsman.menuplanner.presentation.screens.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.menuplanner.domain.usecase.product.GetAllProductsUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.AddToShoppingListUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.GetShoppingListUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.MoveCheckedItemsToFridgeUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.RemoveShoppingItemUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.ToggleShoppingItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    getShoppingList: GetShoppingListUseCase,
    getAllProducts: GetAllProductsUseCase,
    private val addToShoppingList: AddToShoppingListUseCase,
    private val toggleShoppingItem: ToggleShoppingItemUseCase,
    private val removeShoppingItem: RemoveShoppingItemUseCase,
    private val findOrCreateProduct: FindOrCreateProductUseCase,
    private val moveCheckedItemsToFridge: MoveCheckedItemsToFridgeUseCase
) : ViewModel() {

    val items: StateFlow<List<ShoppingListItem>> = getShoppingList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    suspend fun createProduct(name: String, category: Category, unit: MeasureUnit): Product =
        findOrCreateProduct(name, category, unit)

    fun addItem(product: Product, unit: MeasureUnit, quantity: Double) {
        viewModelScope.launch {
            addToShoppingList(product, unit, quantity)
            closePicker()
        }
    }

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch { toggleShoppingItem(item.id, !item.isChecked) }
    }

    fun removeItem(item: ShoppingListItem) {
        viewModelScope.launch { removeShoppingItem(item.id) }
    }

    /** Переносит отмеченные позиции в холодильник и убирает их из списка покупок. */
    fun moveCheckedToFridge() {
        viewModelScope.launch { moveCheckedItemsToFridge() }
    }
}