package com.shvarsman.menuplanner.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeProductsUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.AddProductsToShoppingListUseCase
import com.shvarsman.menuplanner.domain.usecase.shoppinglist.GetShoppingListUseCase
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
    getFridgeProducts: GetFridgeProductsUseCase,
    private val addProductsToShoppingList: AddProductsToShoppingListUseCase,
    private val toggleShoppingItem: ToggleShoppingItemUseCase,
    private val removeShoppingItem: RemoveShoppingItemUseCase
) : ViewModel() {

    val items: StateFlow<List<ShoppingListItem>> = getShoppingList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fridgeProducts: StateFlow<List<Product>> = getFridgeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPickerOpen = MutableStateFlow(false)
    val isPickerOpen: StateFlow<Boolean> = _isPickerOpen

    fun openPicker() { _isPickerOpen.value = true }
    fun closePicker() { _isPickerOpen.value = false }

    fun addFromFridge(products: List<Product>) {
        viewModelScope.launch {
            addProductsToShoppingList(products)
            closePicker()
        }
    }

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch { toggleShoppingItem(item.id, !item.isChecked) }
    }

    fun removeItem(item: ShoppingListItem) {
        viewModelScope.launch { removeShoppingItem(item.id) }
    }
}
