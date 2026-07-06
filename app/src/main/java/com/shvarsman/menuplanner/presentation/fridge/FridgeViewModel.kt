package com.shvarsman.menuplanner.presentation.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.usecase.fridge.AddFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.DeleteFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeItemsUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.UpdateFridgeItemUseCase
import com.shvarsman.menuplanner.domain.usecase.product.FindOrCreateProductUseCase
import com.shvarsman.menuplanner.domain.usecase.product.GetAllProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor(
    getFridgeItems: GetFridgeItemsUseCase,
    getAllProducts: GetAllProductsUseCase,
    private val addFridgeItem: AddFridgeItemUseCase,
    private val updateFridgeItem: UpdateFridgeItemUseCase,
    private val deleteFridgeItem: DeleteFridgeItemUseCase,
    private val findOrCreateProduct: FindOrCreateProductUseCase
) : ViewModel() {

    val items: StateFlow<List<FridgeItem>> = getFridgeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalog: StateFlow<List<Product>> = getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAddPickerOpen = MutableStateFlow(false)
    val isAddPickerOpen: StateFlow<Boolean> = _isAddPickerOpen

    private val _editingItem = MutableStateFlow<FridgeItem?>(null)
    val editingItem: StateFlow<FridgeItem?> = _editingItem

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun openAddPicker() { _isAddPickerOpen.value = true }
    fun closeAddPicker() { _isAddPickerOpen.value = false }

    fun onEditClick(item: FridgeItem) { _editingItem.value = item }
    fun closeEditDialog() { _editingItem.value = null }

    suspend fun createProduct(name: String, category: Category, unit: MeasureUnit): Product =
        findOrCreateProduct(name, category, unit)

    fun addItem(product: Product, unit: MeasureUnit, quantity: Double) {
        viewModelScope.launch {
            try {
                addFridgeItem(FridgeItem(product = product, unit = unit, quantity = quantity))
                closeAddPicker()
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateItemQuantity(item: FridgeItem, unit: MeasureUnit, quantity: Double) {
        viewModelScope.launch {
            try {
                updateFridgeItem(item.copy(unit = unit, quantity = quantity))
                closeEditDialog()
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun onDeleteClick(item: FridgeItem) {
        viewModelScope.launch { deleteFridgeItem(item.id) }
    }

    fun clearError() { _errorMessage.value = null }
}