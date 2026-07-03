package com.shvarsman.menuplanner.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.fridge.ProductIcon

@Composable
fun FridgeIngredientPickerDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Product, Double) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ингредиент из холодильника") },
        text = {
            if (selectedProduct == null) {
                if (products.isEmpty()) {
                    Text("В холодильнике пока нет продуктов.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(products, key = { it.id }) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = { Text("В наличии: ${formatQty(product.quantity)} ${product.unit.displayName}") },
                                leadingContent = { ProductIcon(iconKey = product.iconKey) },
                                modifier = Modifier.clickable {
                                    selectedProduct = product
                                    quantityText = product.quantity.toString()
                                }
                            )
                        }
                    }
                }
            } else {
                val product = selectedProduct!!
                Column {
                    Text("${product.name} (${product.unit.displayName})")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Количество в рецепте") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val product = selectedProduct
            if (product != null) {
                TextButton(onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    onConfirm(product, qty)
                }) { Text("Добавить") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
