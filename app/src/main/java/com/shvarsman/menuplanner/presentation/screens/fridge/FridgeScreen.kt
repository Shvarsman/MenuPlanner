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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

@Composable
fun FridgeScreen(
    modifier: Modifier = Modifier,
    onOpenCatalog: () -> Unit,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val isAddPickerOpen by viewModel.isAddPickerOpen.collectAsStateWithLifecycle()
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val onEditClick = remember(viewModel) { { item: FridgeItem -> viewModel.onEditClick(item) } }
    val onDeleteClick =
        remember(viewModel) { { item: FridgeItem -> viewModel.onDeleteClick(item) } }

    val grouped = remember(items) {
        items.groupBy { it.product.category }.toSortedMap(compareBy { it.ordinal })
    }

    val listState = rememberLazyListState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
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
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text(text = "Поиск в холодильнике") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Поиск"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
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
                                .padding(top = 8.dp, bottom = 8.dp, end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
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
        if (items.isEmpty()) {
            EmptyFridgeState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp
                )
            ) {
                grouped.forEach { (category, categoryItems) ->
                    item(key = "header_${category.name}") {
                        CategoryHeader(category = category)
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        FridgeItemCard(
                            item = item,
                            onEdit = onEditClick,
                            onDelete = onDeleteClick
                        )
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
private fun FridgeItemCard(
    modifier: Modifier = Modifier,
    item: FridgeItem,
    onEdit: (FridgeItem) -> Unit,
    onDelete: (FridgeItem) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductIcon(product = item.product)
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${formatQty(item.quantity)} ${item.unit.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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