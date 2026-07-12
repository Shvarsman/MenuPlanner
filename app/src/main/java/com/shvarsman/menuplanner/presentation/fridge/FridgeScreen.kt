package com.shvarsman.menuplanner.presentation.fridge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.presentation.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    onOpenCatalog: () -> Unit,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val isAddPickerOpen by viewModel.isAddPickerOpen.collectAsState()
    val editingItem by viewModel.editingItem.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val grouped = remember(items) {
        items.groupBy { it.product.category }.toSortedMap(compareBy { it.ordinal })
    }

    val listState = rememberLazyListState()
    var searchBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -2) searchBarVisible = false
                if (available.y > 2) searchBarVisible = true
                return Offset.Zero
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                "Холодильник",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                        actions = {
                            IconButton(onClick = onOpenCatalog) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ListAlt,
                                    contentDescription = "Все продукты"
                                )
                            }
                        }
                    )
                    AnimatedVisibility(
                        visible = searchBarVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Поиск в холодильнике") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Очистить")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(AppCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddPicker() }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyFridgeState(modifier = Modifier
                .fillMaxSize()
                .padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp
                )
            ) {
                grouped.forEach { (category, categoryItems) ->
                    item(key = "header_${category.name}") { CategoryHeader(category) }
                    items(categoryItems, key = { it.id }) { item ->
                        FridgeItemCard(
                            item = item,
                            onEdit = { viewModel.onEditClick(item) },
                            onDelete = { viewModel.onDeleteClick(item) }
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
            title = { Text("Ошибка") },
            text = { Text(message) }
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
private fun FridgeItemCard(item: FridgeItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
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
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatQty(item.quantity)} ${item.unit.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit, contentDescription = "Изменить количество"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete, contentDescription = "Удалить"
                )
            }
        }
    }
}

@Composable
private fun EmptyFridgeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "В холодильнике пока пусто.\nДобавьте первый продукт.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()