package com.shvarsman.menuplanner.presentation.shoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.presentation.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val isPickerOpen by viewModel.isPickerOpen.collectAsState()

    val hasCheckedItems = remember(items) { items.any { it.isChecked } }
    var showMoveConfirmation by remember { mutableStateOf(false) }

    // Группируем по категории продукта; купленные позиции — отдельно, в конце
    val groupedUnchecked = remember(items) {
        items.filter { !it.isChecked }
            .groupBy { it.product.category }
            .toSortedMap(compareBy { it.ordinal })
    }
    val checkedItems = remember(items) { items.filter { it.isChecked } }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Список покупок",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showMoveConfirmation = true },
                        enabled = hasCheckedItems
                    ) {
                        Icon(
                            Icons.Filled.Kitchen,
                            contentDescription = "Перенести купленное в холодильник",
                            tint = if (hasCheckedItems)
                                LocalContentColor.current
                            else
                                LocalContentColor.current.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openPicker() }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                    "Список покупок пуст.\nДобавьте продукты.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp
                )
            ) {
                groupedUnchecked.forEach { (category, categoryItems) ->
                    item(key = "header_${category.name}") { CategoryHeader(category) }
                    items(categoryItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item) },
                            onRemove = { viewModel.removeItem(item) })
                    }
                }

                if (checkedItems.isNotEmpty()) {
                    item(key = "header_checked") { CheckedHeader() }
                    items(checkedItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleChecked(item) },
                            onRemove = { viewModel.removeItem(item) })
                    }
                }
            }
        }
    }

    if (isPickerOpen) {
        ProductPickerDialog(
            catalog = catalog,
            onDismiss = { viewModel.closePicker() },
            onConfirm = { product, unit, qty -> viewModel.addItem(product, unit, qty) },
            onCreateProduct = { name, category, unit ->
                viewModel.createProduct(
                    name,
                    category,
                    unit
                )
            }
        )
    }

    if (showMoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showMoveConfirmation = false },
            title = { Text("Перенести в холодильник?") },
            text = { Text("Отмеченные продукты будут удалены из списка покупок и добавлены в холодильник.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.moveCheckedToFridge()
                    showMoveConfirmation = false
                }) { Text("Перенести") }
            },
            dismissButton = {
                TextButton(onClick = { showMoveConfirmation = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun CategoryHeader(category: Category) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CheckedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Куплено",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingListItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Icon(
                item.product.category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${formatQty(item.quantity)} ${item.unit.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить из списка"
                )
            }
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()