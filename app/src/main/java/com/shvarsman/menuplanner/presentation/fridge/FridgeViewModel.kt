package com.shvarsman.menuplanner.presentation.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.usecase.fridge.AddProductUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.DeleteProductUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.GetFridgeProductsUseCase
import com.shvarsman.menuplanner.domain.usecase.fridge.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FridgeUiState(
    val products: List<Product> = emptyList(),
    val isEditorOpen: Boolean = false,
    val editingProduct: Product? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FridgeViewModel @Inject constructor(
    getFridgeProducts: GetFridgeProductsUseCase,
    private val addProduct: AddProductUseCase,
    private val updateProduct: UpdateProductUseCase,
    private val deleteProduct: DeleteProductUseCase
) : ViewModel() {

    val products: StateFlow<List<Product>> = getFridgeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct

    private val _editorOpen = MutableStateFlow(false)
    val editorOpen: StateFlow<Boolean> = _editorOpen

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onAddClick() = openEditor(null)
    fun onEditClick(product: Product) = openEditor(product)

    private fun openEditor(product: Product?) {
        _editingProduct.value = product
        _editorOpen.value = true
    }

    fun closeEditor() {
        _editorOpen.value = false
        _editingProduct.value = null
    }

    fun saveProduct(name: String, category: Category, unit: MeasureUnit, quantity: Double) {
        viewModelScope.launch {
            try {
                val current = _editingProduct.value
                if (current == null) {
                    addProduct(Product(name = name, category = category, unit = unit, quantity = quantity))
                } else {
                    updateProduct(current.copy(name = name, category = category, unit = unit, quantity = quantity))
                }
                closeEditor()
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun onDeleteClick(product: Product) {
        viewModelScope.launch { deleteProduct(product.id) }
    }

    fun clearError() { _errorMessage.value = null }
}
