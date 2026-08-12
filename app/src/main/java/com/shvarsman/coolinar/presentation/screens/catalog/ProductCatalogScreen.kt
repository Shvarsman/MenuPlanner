@file:OptIn(ExperimentalMaterial3Api::class)

package com.shvarsman.coolinar.presentation.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.presentation.screens.common.AppBottomSheet
import com.shvarsman.coolinar.presentation.screens.common.DropdownFilterChip
import com.shvarsman.coolinar.presentation.screens.common.ProductFormFields
import com.shvarsman.coolinar.presentation.screens.common.TopBarSearchField
import com.shvarsman.coolinar.presentation.screens.common.localizedName
import com.shvarsman.coolinar.presentation.ui.icons.CategoryIcon
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.GroupedRow
import com.shvarsman.coolinar.presentation.utils.rememberDebouncedSearch
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedFromCatalogTemplate = stringResource(R.string.deleted_from_catalog)
    val undoLabel = stringResource(R.string.undo)

    fun requestDelete(product: Product) {
        scope.launch {
            val deleted = viewModel.requestDelete(product)
            if (!deleted) return@launch
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = String.format(deletedFromCatalogTemplate, product.name),
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(product.id)
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clip(CornerShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                CornerShape
                            )
                            .gradientStyle(shape = CornerShape),
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    TopBarSearchField(
                        modifier = Modifier.padding(end = 16.dp, start = 8.dp),
                        query = localSearchQuery,
                        onQueryChange = onLocalSearchQueryChange,
                        placeholder = stringResource(R.string.search_products)
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
                    label = { Text(stringResource(R.string.my_products)) },
                    shape = CornerShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surface,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                FilterChip(
                    selected = !showOnlyCustom,
                    onClick = { viewModel.toggleShowOnlyCustom() },
                    label = { Text(stringResource(R.string.all)) },
                    shape = CornerShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surface,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                DropdownFilterChip(
                    displayText = selectedCategory?.labelRes?.let { stringResource(it) }
                        ?: stringResource(R.string.category_label),
                    isActive = selectedCategory != null
                ) { close ->
                    DropdownMenuItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = { Text(stringResource(R.string.all_categories)) },
                        onClick = { viewModel.selectCategory(null); close() }
                    )
                    if (availableCategories.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                        availableCategories.forEach { (category, count) ->
                            DropdownMenuItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.category_with_count,
                                            stringResource(category.labelRes),
                                            count
                                        ),
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
                        if (searchQuery.isNotBlank()) stringResource(R.string.nothing_found)
                        else if (showOnlyCustom) stringResource(R.string.no_custom_products)
                        else stringResource(R.string.catalog_empty),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
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
            onConfirm = { name, category, unit, isToTaste, isAlwaysAvailable ->
                viewModel.saveEdit(
                    name,
                    category,
                    unit,
                    isToTaste,
                    isAlwaysAvailable
                )
            }
        )
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text(text = stringResource(R.string.delete_product_title)) },
            text = { Text(text = stringResource(R.string.delete_product_message, product.name)) },
            confirmButton = {
                TextButton(onClick = {
                    requestDelete(product)
                    productPendingDelete = null
                }) { Text(text = stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    productPendingDelete = null
                }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }

    pendingForceDelete?.let { (product, usagesCount) ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelForceDelete() },
            title = { Text(text = stringResource(R.string.product_in_use_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.product_in_use_message,
                        product.name,
                        usagesCount
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmForceDelete() }) {
                    Text(
                        text = stringResource(
                            R.string.delete_anyway
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelForceDelete() }) {
                    Text(
                        text = stringResource(
                            R.string.cancel
                        )
                    )
                }
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
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CatalogProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = product.localizedName()) },
        supportingContent = {
            Text(
                text = stringResource(
                    R.string.default_unit,
                    stringResource(product.defaultUnit.labelRes)
                )
            )
        },
        leadingContent = {
            ProductIcon(product = product, modifier = Modifier.size(32.dp))
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.edit),
                        modifier = Modifier.size(20.dp),
                        contentDescription = stringResource(R.string.edit)
                    )
                }
                if (!product.isDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.delete),
                            modifier = Modifier.size(20.dp),
                            contentDescription = stringResource(R.string.delete_from_catalog)
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            headlineColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun EditProductBottomSheet(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: Category, unit: MeasureUnit, isToTaste: Boolean, isAlwaysAvailable: Boolean) -> Unit
) {
    var name by remember(product.id) { mutableStateOf(product.name) }
    var category by remember(product.id) { mutableStateOf(product.category) }
    var unit by remember(product.id) { mutableStateOf(product.defaultUnit) }
    var isToTaste by remember(product.id) { mutableStateOf(product.isToTaste) }
    var isAlwaysAvailable by remember(product.id) { mutableStateOf(product.isAlwaysAvailable) }

    AppBottomSheet(
        title = stringResource(R.string.edit_product_title),
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { onClose ->
        ProductFormFields(
            name = name,
            onNameChange = { name = it },
            category = category,
            onCategoryChange = { category = it },
            unit = unit,
            onUnitChange = { unit = it },
            isToTaste = isToTaste,
            onIsToTasteChange = { isToTaste = it },
            isAlwaysAvailable = isAlwaysAvailable,
            onIsAlwaysAvailableChange = { isAlwaysAvailable = it },
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), category, unit, isToTaste, isAlwaysAvailable) }
            ) { Text(stringResource(R.string.save)) }
        }
    }
}