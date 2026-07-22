package com.shvarsman.menuplanner.presentation.screens.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.screens.common.DropdownFilterChip
import com.shvarsman.menuplanner.presentation.screens.common.TopBarSearchField
import com.shvarsman.menuplanner.presentation.screens.fridge.ProductIcon
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: ProductCatalogViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showOnlyCustom by viewModel.showOnlyCustom.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()
    val pendingForceDelete by viewModel.pendingForceDelete.collectAsStateWithLifecycle()
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }

    val lazyListState = rememberLazyListState()
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                title = {
                    TopBarSearchField(
                        query = localSearchQuery,
                        onQueryChange = onLocalSearchQueryChange,
                        placeholder = "Поиск продуктов"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showOnlyCustom,
                    onClick = { viewModel.toggleShowOnlyCustom() },
                    label = { Text("Мои продукты") }
                )
                FilterChip(
                    selected = !showOnlyCustom,
                    onClick = { viewModel.toggleShowOnlyCustom() },
                    label = { Text("Все") }
                )
                DropdownFilterChip(
                    displayText = selectedCategory?.displayName ?: "Категория",
                    isActive = selectedCategory != null
                ) { close ->
                    DropdownMenuItem(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = { Text("Все категории") },
                        onClick = { viewModel.selectCategory(null); close() }
                    )
                    if (availableCategories.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        availableCategories.forEach { (category, count) ->
                            DropdownMenuItem(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = {
                                    Text(
                                        "${category.displayName} ($count)",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    CategoryIcon(
                                        category = category,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (category == selectedCategory) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { viewModel.selectCategory(category); close() },
                            )
                        }
                    }

                }
            }

            if (listState.isEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (searchQuery.isNotBlank()) "Ничего не найдено"
                        else if (showOnlyCustom) "Вы ещё не добавили свои продукты"
                        else "Каталог продуктов пуст.\nПродукты появятся здесь после добавления\nв холодильник, рецепт или список покупок.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp)
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
                                is GroupedRow.Item -> "product"
                            }
                        }
                    ) { row ->
                        when (row) {
                            is GroupedRow.Header -> CatalogCategoryHeader(category = row.category)
                            is GroupedRow.Item -> CatalogProductRow(
                                product = row.value,
                                onEdit = { viewModel.startEdit(row.value) },
                                onDelete = { productPendingDelete = row.value }
                            )
                        }
                    }
                }
            }
        }
    }

    editingProduct?.let { product ->
        EditProductBottomSheet(
            product = product,
            onDismiss = { viewModel.cancelEdit() },
            onConfirm = { name, category, unit -> viewModel.saveEdit(name, category, unit) }
        )
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text(text = "Удалить продукт?") },
            text = { Text(text = "«${product.name}» будет удалён из каталога.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(product)
                    productPendingDelete = null
                }) { Text(text = "Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { productPendingDelete = null }) { Text(text = "Отмена") }
            }
        )
    }

    pendingForceDelete?.let { (product, usagesCount) ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelForceDelete() },
            title = { Text(text = "Продукт используется") },
            text = {
                Text(
                    text = "«${product.name}» используется в $usagesCount местах " +
                            "(рецепты / холодильник / список покупок). При удалении эти записи " +
                            "тоже будут удалены. Продолжить?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmForceDelete() }) { Text(text = "Всё равно удалить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelForceDelete() }) { Text(text = "Отмена") }
            }
        )
    }
}

@Composable
private fun CatalogCategoryHeader(category: Category) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryIcon(category = category, modifier = Modifier.size(20.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CatalogProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = product.name) },
        supportingContent = { Text(text = "По умолчанию: ${product.defaultUnit.displayName}") },
        leadingContent = {
            ProductIcon(product = product, modifier = Modifier.size(32.dp))
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Редактировать")
                }
                if (!product.isDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить из каталога"
                        )
                    }
                }
            }
        },
    )
}