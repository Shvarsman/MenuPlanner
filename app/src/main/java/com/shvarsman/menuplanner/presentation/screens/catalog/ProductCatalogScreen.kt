package com.shvarsman.menuplanner.presentation.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.presentation.screens.fridge.ProductIcon
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: ProductCatalogViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }

    val lazyListState = rememberLazyListState()

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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
                SearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholderText = "Поиск продукта",
                    searchQuery = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChange(it) }
                )
            }
        }
    ) { padding ->
        if (listState.isEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (searchQuery.isNotBlank()) "Ничего не найдено"
                    else "Каталог продуктов пуст.\nПродукты появятся здесь после добавления\nв холодильник, рецепт или список покупок.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp
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
                            is GroupedRow.Item -> "product"
                        }
                    }
                ) { row ->
                    when (row) {
                        is GroupedRow.Header -> CatalogCategoryHeader(category = row.category)
                        is GroupedRow.Item -> CatalogProductRow(
                            product = row.value,
                            onDelete = { productPendingDelete = row.value }
                        )
                    }
                }
            }
        }
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text(text = "Удалить продукт?") },
            text = {
                Text(
                    text = "«${product.name}» будет удалён из каталога вместе со всеми записями " +
                            "в холодильнике, рецептах и списке покупок, где он используется."
                )
            },
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
private fun CatalogProductRow(product: Product, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = product.name) },
        supportingContent = { Text(text = "По умолчанию: ${product.defaultUnit.displayName}") },
        leadingContent = {
            ProductIcon(
                product = product,
                modifier = Modifier.size(32.dp)
            )
        },
        trailingContent = {
            if (!product.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить из каталога"
                    )
                }
            }
        },
    )
}
