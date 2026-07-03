package com.shvarsman.menuplanner.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.Product

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
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = {
                                    Text("В наличии: ${formatQty(product.quantity)} ${product.unit.displayName}")
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = product.category.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = product.category.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${product.name} (${product.unit.displayName})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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