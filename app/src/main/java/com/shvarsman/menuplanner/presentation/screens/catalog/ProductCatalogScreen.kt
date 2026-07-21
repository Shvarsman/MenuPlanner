package com.shvarsman.menuplanner.presentation.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.Product
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

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = "Каталог продуктов") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )

                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = localSearchQuery,
                            onQueryChange = onLocalSearchQueryChange,
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text("Поиск продукта") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    content = {}
                )

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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
                }

                if (availableCategories.isNotEmpty()) {
                    CategoryFilterSection(
                        categories = availableCategories,
                        selectedCategory = selectedCategory,
                        onCategoryClick = { viewModel.selectCategory(it) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) { padding ->
        if (listState.isEmpty) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (searchQuery.isNotBlank()) "Ничего не найдено"
                    else if (showOnlyCustom) "Вы ещё не добавили свои продукты"
                    else "Каталог продуктов пуст.\nПродукты появятся здесь после добавления\nв холодильник, рецепт или список покупок.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
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

/**
 * Ряд чипов категорий: изначально показаны первые несколько (с иконкой и числом
 * продуктов), остальные скрыты за чипом "Ещё N" — по нажатию раскрываются в
 * многострочный FlowRow. Экономит вертикальное пространство при 20+ категориях,
 * не заставляя пользователя сразу листать длинный ряд.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterSection(
    categories: List<Pair<Category, Int>>,
    selectedCategory: Category?,
    onCategoryClick: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val collapsedCount = 6

    val visibleCategories = if (isExpanded || categories.size <= collapsedCount) {
        categories
    } else {
        categories.take(collapsedCount)
    }
    val hiddenCount = categories.size - visibleCategories.size

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategoryClick(selectedCategory ?: return@FilterChip) },
            label = { Text("Все категории") }
        )

        visibleCategories.forEach { (category, count) ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategoryClick(category) },
                label = { Text("${category.displayName} ($count)") },
                leadingIcon = {
                    CategoryIcon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        category = category
                    )
                }
            )
        }

        if (hiddenCount > 0 || isExpanded) {
            AssistChip(
                onClick = { isExpanded = !isExpanded },
                label = { Text(if (isExpanded) "Свернуть" else "Ещё $hiddenCount") },
                trailingIcon = {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )
        }
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
        CategoryIcon(
            category = category,
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
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Удалить из каталога")
                    }
                }
            }
        },
    )
}