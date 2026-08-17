package com.shvarsman.coolinar.presentation.screens.shoppinglist

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.presentation.screens.common.AppSnackbarHost
import com.shvarsman.coolinar.presentation.screens.common.DropdownFilterChip
import com.shvarsman.coolinar.presentation.screens.common.FieldLabel
import com.shvarsman.coolinar.presentation.screens.common.GlassFab
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.MascotEmptyState
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotPose
import com.shvarsman.coolinar.presentation.screens.common.MascotWelcomeTip
import com.shvarsman.coolinar.presentation.screens.common.ProductPickerDialog
import com.shvarsman.coolinar.presentation.screens.common.QuantityUnitField
import com.shvarsman.coolinar.presentation.screens.common.SwipeToDeleteRow
import com.shvarsman.coolinar.presentation.screens.common.TopBarSearchField
import com.shvarsman.coolinar.presentation.screens.common.localizedName
import com.shvarsman.coolinar.presentation.ui.icons.CategoryIcon
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val groupedUnchecked by viewModel.groupedUnchecked.collectAsStateWithLifecycle()
    val checkedItems by viewModel.checkedItems.collectAsStateWithLifecycle()
    val hasCheckedItems by viewModel.hasCheckedItems.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val isPickerOpen by viewModel.isPickerOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()
    val showMoveConfirmation by viewModel.showMoveConfirmation.collectAsStateWithLifecycle()
    val moveCompleted by viewModel.moveCompleted.collectAsStateWithLifecycle()
    val isEmpty = groupedUnchecked.isEmpty() && checkedItems.isEmpty()

    MascotWelcomeTip(
        tipId = "shoppinglist_intro",
        message = stringResource(R.string.mascot_tip_shoppinglist),
        enabled = !isEmpty
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedItemTemplate = stringResource(R.string.shopping_item_deleted)
    val undoLabel = stringResource(R.string.undo)

    fun requestDelete(item: ShoppingListItem) {
        viewModel.requestDelete(item.id)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = String.format(deletedItemTemplate, item.product.name),
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(item.id)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    TopBarSearchField(
                        modifier = Modifier.padding(end = 8.dp),
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = stringResource(R.string.search_shopping_list)
                    )
                },
                actions = {
                    GlassIconButton(
                        onClick = { viewModel.requestMoveCheckedToFridge() },
                        color = if (hasCheckedItems) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.fridge),
                            modifier = Modifier.size(20.dp),
                            contentDescription = stringResource(R.string.move_to_fridge),
                            tint = if (hasCheckedItems) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            GlassFab(
                onClick = { viewModel.openPicker() }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_product)
                )
            }
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
                DropdownFilterChip(
                    displayText = selectedCategory?.labelRes?.let { stringResource(it) }
                        ?: stringResource(R.string.category_label),
                    isActive = selectedCategory != null
                ) { close ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.all_categories)) },
                        onClick = { viewModel.selectCategory(null); close() }
                    )
                    availableCategories.forEach { (category, count) ->
                        DropdownMenuItem(
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
                                if (category == selectedCategory) Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = { viewModel.selectCategory(category); close() }
                        )
                    }
                }

                DropdownFilterChip(
                    displayText = stringResource(sortOption.displayNameRes),
                    isActive = sortOption != ShoppingSortOption.NAME_ASC
                ) { close ->
                    ShoppingSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.displayNameRes)) },
                            trailingIcon = {
                                if (option == sortOption) Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = { viewModel.selectSortOption(option); close() }
                        )
                    }
                }
            }


            if (isEmpty) {
                val isFiltering = searchQuery.isNotBlank() || selectedCategory != null
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    MascotEmptyState(
                        pose = if (isFiltering) MascotPose.SEARCHING else MascotPose.SAD,
                        title = if (isFiltering) stringResource(R.string.nothing_found)
                        else stringResource(R.string.shopping_list_empty)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = padding.calculateBottomPadding()
                    )
                ) {
                    groupedUnchecked.forEach { (category, categoryItems) ->
                        item(key = "header_${category.name}") { CategoryHeader(category) }
                        items(categoryItems, key = { it.id }) { item ->
                            SwipeToDeleteRow(key = item.id, onDelete = { requestDelete(item) }) {
                                ShoppingItemRow(
                                    item = item,
                                    onToggleChecked = { viewModel.toggleChecked(item) },
                                    onLongClick = { viewModel.startEdit(item) }
                                )
                            }
                        }
                    }

                    if (checkedItems.isNotEmpty()) {
                        item(key = "header_checked") { CheckedHeader() }
                        items(checkedItems, key = { it.id }) { item ->
                            SwipeToDeleteRow(key = item.id, onDelete = { requestDelete(item) }) {
                                ShoppingItemRow(
                                    item = item,
                                    onToggleChecked = { viewModel.toggleChecked(item) },
                                    onLongClick = { viewModel.startEdit(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isPickerOpen) {
        ProductPickerDialog(
            catalog = catalog,
            onDismiss = { viewModel.closePicker() },
            onConfirm = { product, unit, qty, _ -> viewModel.addItem(product, unit, qty) },
            onCreateProduct = { name, category, unit, isToTaste, isAlwaysAvailable ->
                viewModel.createProduct(name, category, unit, isToTaste, isAlwaysAvailable)
            },
            showExpirationDate = false
        )
    }

    editingItem?.let { item ->
        EditShoppingItemDialog(
            item = item,
            onDismiss = { viewModel.cancelEdit() },
            onConfirm = { unit, qty -> viewModel.confirmEdit(unit, qty) }
        )
    }

    if (showMoveConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelMoveToFridge() },
            title = { Text(stringResource(R.string.move_to_fridge_title)) },
            text = { Text(stringResource(R.string.move_to_fridge_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmMoveCheckedToFridge() }) {
                    Text(
                        stringResource(R.string.move)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelMoveToFridge() }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }

    if (moveCompleted) {
        LaunchedEffect(moveCompleted) {
            delay(1600.milliseconds)
            viewModel.dismissMoveCompleted()
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissMoveCompleted() },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MascotImage(
                        pose = MascotPose.EXCITED,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.moved_to_fridge_success),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}

@Composable
private fun CategoryHeader(category: Category) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryIcon(
            modifier = Modifier.size(20.dp),
            category = category
        )
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CheckedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.purchased),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingListItem,
    onToggleChecked: () -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onToggleChecked,
            onLongClick = onLongClick
        ),
        headlineContent = {
            Text(
                text = item.product.localizedName(),
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isChecked, onCheckedChange = { onToggleChecked() })
                Spacer(Modifier.width(8.dp))
                ProductIcon(product = item.product, modifier = Modifier.size(32.dp))
            }
        },
        trailingContent = {
            Text(
                text = stringResource(
                    R.string.qty_with_unit,
                    formatQty(item.quantity),
                    stringResource(item.unit.labelRes)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            headlineColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun EditShoppingItemDialog(
    item: ShoppingListItem,
    onDismiss: () -> Unit,
    onConfirm: (unit: MeasureUnit, quantity: Double) -> Unit
) {
    var quantityText by remember { mutableStateOf(formatQty(item.quantity)) }
    var selectedUnit by remember { mutableStateOf(item.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.product.name) },
        text = {
            Column {
                FieldLabel(stringResource(R.string.quantity_label))
                QuantityUnitField(
                    quantityText = quantityText,
                    onQuantityChange = { quantityText = it },
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedUnit, quantityText.toDoubleOrNull() ?: item.quantity)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()