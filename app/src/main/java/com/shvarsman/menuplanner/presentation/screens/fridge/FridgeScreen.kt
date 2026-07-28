@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.shvarsman.menuplanner.presentation.screens.fridge

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.presentation.screens.common.DropdownFilterChip
import com.shvarsman.menuplanner.presentation.screens.common.GlassFab
import com.shvarsman.menuplanner.presentation.screens.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.screens.common.TopBarSearchField
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.ProductIcon
import com.shvarsman.menuplanner.presentation.ui.theme.CornerShape
import com.shvarsman.menuplanner.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.menuplanner.presentation.ui.theme.gradientStyle
import com.shvarsman.menuplanner.presentation.utils.GroupedRow
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch
import com.shvarsman.menuplanner.presentation.utils.rememberOptimisticDelete
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun FridgeScreen(
    modifier: Modifier = Modifier,
    onOpenCatalog: () -> Unit,
    onSelectionModeChange: (Boolean) -> Unit = {},
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val isAddPickerOpen by viewModel.isAddPickerOpen.collectAsStateWithLifecycle()
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val groupByCategory by viewModel.groupByCategory.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    val isSelectionMode = selectedIds.isNotEmpty()
    LaunchedEffect(isSelectionMode) {
        onSelectionModeChange(isSelectionMode)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val requestDelete = rememberOptimisticDelete<FridgeItem, Long>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { item -> "«${item.product.name}» удалён" },
        onRequestDelete = { id -> viewModel.requestDelete(id) },
        onUndo = { id -> viewModel.undoDelete(id) }
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    if (isSelectionMode) {
                        Text("Выбрано: ${selectedIds.size}")
                    } else {
                        TopBarSearchField(
                            modifier = Modifier.padding(end = 8.dp),
                            query = localSearchQuery,
                            onQueryChange = onLocalSearchQueryChange,
                            placeholder = "Поиск в холодильнике"
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    RoundedCornerShape(28.dp)
                                )
                                .gradientStyle(shape = RoundedCornerShape(28.dp)),
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Закрыть выбор")
                        }
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        IconButton(
                            onClick = onOpenCatalog,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(CornerShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    CornerShape
                                )
                                .gradientStyle(shape = CornerShape),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Все продукты")
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
            if (!isSelectionMode) {
                GlassFab(
                    onClick = { viewModel.openAddPicker() },
                    modifier = Modifier.padding(bottom = FloatingBottomBarClearance)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                BottomAppBar {
                    TextButton(onClick = { viewModel.selectAll() }) {
                        Icon(Icons.Filled.SelectAll, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Выбрать все")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleFavoriteSelected() }) {
                        Icon(Icons.Filled.Star, contentDescription = "Избранное")
                    }
                    IconButton(onClick = {
                        val ids = selectedIds.toList()
                        viewModel.clearSelection()
                        viewModel.requestDeleteBulk(ids)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Удалено продуктов: ${ids.size}",
                                actionLabel = "Отменить",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) viewModel.undoDeleteBulk(
                                ids
                            )
                        }
                    }) { Icon(Icons.Filled.Delete, contentDescription = "Удалить") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DropdownFilterChip(
                        displayText = selectedCategory?.displayName ?: "Категория",
                        isActive = selectedCategory != null
                    ) { close ->
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = { Text("Все категории") },
                            onClick = { viewModel.selectCategory(null); close() }
                        )
                        availableCategories.forEach { (category, count) ->
                            DropdownMenuItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
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
                        modifier = Modifier.widthIn(max = 230.dp),
                        displayText = sortOption.displayName,
                        isActive = sortOption != FridgeSortOption.NAME_ASC
                    ) { close ->
                        FridgeSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = { Text(option.displayName) },
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

                    FilterChip(
                        selected = groupByCategory,
                        onClick = { viewModel.toggleGroupByCategory() },
                        label = { Text("По категориям") },
                        shape = CornerShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surface,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                listState.isEmpty -> {
                    EmptyFridgeState(modifier = Modifier.fillMaxSize())
                }

                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = padding.calculateBottomPadding() +
                                    (if (isSelectionMode) 16.dp else FloatingBottomBarClearance)
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
                                    isSelectionMode = isSelectionMode,
                                    isSelected = row.value.id in selectedIds,
                                    onClick = {
                                        if (isSelectionMode) viewModel.toggleSelection(row.value.id) else viewModel.onEditClick(
                                            row.value
                                        )
                                    },
                                    onLongClick = { viewModel.enterSelectionMode(row.value.id) },
                                    onEdit = { viewModel.onEditClick(row.value) },
                                    onDelete = { requestDelete(row.value) },
                                    onToggleFavorite = { viewModel.toggleFavorite(row.value) },
                                    onSelect = { viewModel.enterSelectionMode(row.value.id) }
                                )
                            }
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
            onConfirm = { product, unit, qty, date -> viewModel.addItem(product, unit, qty, date) },
            onCreateProduct = { name, category, unit, isToTaste, isAlwaysAvailable ->
                viewModel.createProduct(name, category, unit, isToTaste, isAlwaysAvailable)
            }
        )
    }
    editingItem?.let { item ->
        FridgeItemQuantityDialog(
            item = item,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { unit, qty, date -> viewModel.updateItemQuantity(item, unit, qty, date) }
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
private fun CategoryHeader(modifier: Modifier = Modifier, category: Category) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryIcon(category = category, modifier = Modifier.size(20.dp))
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}


@Composable
private fun FridgeItemRow(
    item: FridgeItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelect: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val latestOnDelete = rememberUpdatedState(onDelete)
    val latestIsSelectionMode = rememberUpdatedState(isSelectionMode)
    val confirmValueChange = remember(item.id) {
        { value: SwipeToDismissBoxValue ->
            if (value == SwipeToDismissBoxValue.EndToStart && !latestIsSelectionMode.value) {
                latestOnDelete.value()
                true
            } else {
                false
            }
        }
    }
    val density = LocalDensity.current
    val dismissState = remember(item.id) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = confirmValueChange,
            positionalThreshold = { totalDistance -> totalDistance * 0.5f }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
            headlineContent = {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                item.expirationDate?.let { date -> ExpirationBadge(date) }
            },
            leadingContent = {
                if (isSelectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                } else {
                    ProductIcon(product = item.product)
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.product.isToTaste) {
                        Text(
                            text = "${formatQty(item.quantity)} ${item.unit.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    if (!isSelectionMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isFavorite) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Избранное",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Действия")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier
                                        .clip(CornerShape)
                                        .gradientStyle(),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    shape = CornerShape,
                                    shadowElevation = 0.dp
                                ) {
                                    DropdownMenuItem(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = { Text("Изменить") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = { menuExpanded = false; onEdit() }
                                    )
                                    DropdownMenuItem(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = { Text(if (item.isFavorite) "Убрать из избранного" else "Добавить в избранное") },
                                        leadingIcon = {
                                            Icon(
                                                if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = { menuExpanded = false; onToggleFavorite() }
                                    )
                                    DropdownMenuItem(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = { Text("Удалить") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = { menuExpanded = false; onDelete() },
                                        colors = MenuItemColors(
                                            textColor = MaterialTheme.colorScheme.error,
                                            leadingIconColor = MaterialTheme.colorScheme.error,
                                            trailingIconColor = MaterialTheme.colorScheme.error,
                                            disabledTextColor = MaterialTheme.colorScheme.error,
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.error,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                                    DropdownMenuItem(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = { Text("Выбрать") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.SelectAll,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = { menuExpanded = false; onSelect() }
                                    )
                                }
                            }
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
}

@Composable
private fun ExpirationBadge(date: LocalDate) {
    val today = LocalDate.now()
    val daysLeft = today.until(date).days
    val color = when {
        date.isBefore(today) -> MaterialTheme.colorScheme.error
        daysLeft <= 3 -> MaterialTheme.colorScheme.error
        daysLeft <= 7 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val text = when {
        date.isBefore(today) -> "Срок истёк ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
        else -> "До ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
    }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
private fun EmptyFridgeState(modifier: Modifier = Modifier) {
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
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()