@file:OptIn(ExperimentalMaterial3Api::class)

package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.presentation.screens.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch

@Composable
fun FridgeScreen(
    modifier: Modifier = Modifier,
    onOpenCatalog: () -> Unit,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val isAddPickerOpen by viewModel.isAddPickerOpen.collectAsStateWithLifecycle()
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }
    val onEditClick = remember(viewModel) { { item: FridgeItem -> viewModel.onEditClick(item) } }
    val onDeleteClick =
        remember(viewModel) { { item: FridgeItem -> viewModel.onDeleteClick(item) } }

    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Холодильник",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = onOpenCatalog) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                contentDescription = "Все продукты"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddPicker() }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить продукт"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = onLocalSearchQueryChange,
                placeholder = { Text(text = "Поиск в холодильнике") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Поиск"
                    )
                },
                trailingIcon = {
                    if (localSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onLocalSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Очистить"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(AppCornerRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (listState.isEmpty) {
                EmptyFridgeState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = padding.calculateBottomPadding() + 88.dp
                    )
                ) {
                    items(
                        items = listState.rows,
                        key = { row ->
                            when (row) {
                                is GroupedRow.Header -> "header_${row.category.name}"
                                is GroupedRow.Item -> "item_${row.value.id}"
                            }
                        },
                        contentType = { row ->
                            when (row) {
                                is GroupedRow.Header -> "header"
                                is GroupedRow.Item -> "item"
                            }
                        }
                    ) { row ->
                        when (row) {
                            is GroupedRow.Header -> CategoryHeader(category = row.category)
                            is GroupedRow.Item -> FridgeItemRow(
                                item = row.value,
                                onEdit = onEditClick,
                                onDelete = onDeleteClick
                            )
                        }
                    }
                }
            }
        }
    }

    if (isAddPickerOpen) {
        ProductPickerDialog(
            catalog = catalog,
            onDismiss = { viewModel.closeAddPicker() },
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
    editingItem?.let { item ->
        FridgeItemQuantityDialog(
            item = item,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { unit, qty -> viewModel.updateItemQuantity(item, unit, qty) }
        )
    }
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("Ок") } },
            title = { Text(text = "Ошибка") },
            text = { Text(text = message) }
        )
    }
}

@Composable
private fun CategoryHeader(
    modifier: Modifier = Modifier,
    category: Category
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FridgeItemRow(
    item: FridgeItem,
    onEdit: (FridgeItem) -> Unit,
    onDelete: (FridgeItem) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = "${formatQty(item.quantity)} ${item.unit.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            ProductIcon(product = item.product)
        },
        trailingContent = {
            Row {
                IconButton(onClick = { onEdit(item) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Изменить количество"
                    )
                }
                IconButton(onClick = { onDelete(item) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить"
                    )
                }
            }
        }
    )
}

@Composable
private fun EmptyFridgeState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "В холодильнике пока пусто.\nДобавьте первый продукт.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
