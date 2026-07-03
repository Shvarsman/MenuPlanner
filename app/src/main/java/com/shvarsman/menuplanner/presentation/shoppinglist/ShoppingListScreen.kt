package com.shvarsman.menuplanner.presentation.shoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    val fridgeProducts by viewModel.fridgeProducts.collectAsState()
    val isPickerOpen by viewModel.isPickerOpen.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Список покупок") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openPicker() }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить из холодильника")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Список покупок пуст.\nДобавьте продукты из холодильника.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { viewModel.toggleChecked(item) },
                        onRemove = { viewModel.removeItem(item) }
                    )
                }
            }
        }
    }

    if (isPickerOpen) {
        FridgeMultiPickerDialog(
            products = fridgeProducts,
            onDismiss = { viewModel.closePicker() },
            onConfirm = { selected -> viewModel.addFromFridge(selected) }
        )
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingListItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${formatQty(item.quantity)} ${item.unit.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить из списка")
            }
        }
    }
}

@Composable
private fun FridgeMultiPickerDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (List<Product>) -> Unit
) {
    val selected = remember { mutableStateMapOf<Long, Product>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить из холодильника") },
        text = {
            if (products.isEmpty()) {
                Text("В холодильнике пока нет продуктов.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(products, key = { it.id }) { product ->
                        val isSelected = selected.containsKey(product.id)
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = {
                                Text("${formatQty(product.quantity)} ${product.unit.displayName}")
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = product.category.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selected[product.id] = product
                                        else selected.remove(product.id)
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                if (isSelected) selected.remove(product.id)
                                else selected[product.id] = product
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.values.toList()) }) {
                Text("Добавить (${selected.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()