package com.shvarsman.coolinar.presentation.screens.recipe.list

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.DropdownFilterChip
import com.shvarsman.coolinar.presentation.screens.common.GlassFab
import com.shvarsman.coolinar.presentation.screens.common.TopBarSearchField
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCarouselSection
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCategoryCarousel
import com.shvarsman.coolinar.presentation.screens.recipe.components.recipeGroupedItems
import com.shvarsman.coolinar.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.rememberDebouncedSearch
import com.shvarsman.coolinar.presentation.utils.rememberOptimisticDelete
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onViewRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    onCategoryClick: (RecipeCategory) -> Unit,
    onShowAllCategories: () -> Unit,
    onShowAllSuggested: () -> Unit,
    onShowAllRecipes: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val grouped by viewModel.groupedRecipes.collectAsStateWithLifecycle()
    val suggested by viewModel.suggestedRecipes.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val selectedCookingMethod by viewModel.selectedCookingMethod.collectAsStateWithLifecycle()
    val availableCookingMethods by viewModel.availableCookingMethods.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val groupingOption by viewModel.groupingOption.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isFiltering = searchQuery.isNotBlank() || selectedCategory != null || selectedCookingMethod != null
    val isSelectionMode = selectedIds.isNotEmpty()
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }

    var viewMode by rememberSaveable { mutableStateOf(RecipeViewMode.PHOTO_CARDS) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recipeDeletedTemplate = stringResource(R.string.recipe_deleted)
    val itemsDeletedCountTemplate = stringResource(R.string.items_deleted_count)
    val undoLabel = stringResource(R.string.undo)

    val snackbarHostState = remember { SnackbarHostState() }
    val requestDelete = rememberOptimisticDelete<RecipeSummary, String>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { recipe -> String.format(recipeDeletedTemplate, recipe.title) },
        undoLabel = undoLabel,
        onDelete = { id -> viewModel.requestDelete(id) },
        onUndo = { id -> viewModel.undoDelete(id) }
    )

    val onShare: (RecipeSummary) -> Unit = { recipe ->
        ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(recipe.title)
            .startChooser()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    if (isSelectionMode) {
                        Text(
                            text = stringResource(R.string.selected_count, selectedIds.size),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        TopBarSearchField(
                            modifier = Modifier.padding(end = 16.dp),
                            query = localSearchQuery,
                            onQueryChange = onLocalSearchQueryChange,
                            placeholder = stringResource(R.string.search_recipes)
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clip(CornerShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    CornerShape
                                )
                                .gradientStyle(shape = CornerShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_selection)
                            )
                        }
                    }
                },
                actions = {
                    if (isFiltering && !isSelectionMode) {
                        IconButton(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(CornerShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    CornerShape
                                )
                                .gradientStyle(shape = CornerShape),
                            onClick = {
                                viewMode = if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                                    RecipeViewMode.LIST
                                } else {
                                    RecipeViewMode.PHOTO_CARDS
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                                    ImageVector.vectorResource(R.drawable.view1)
                                } else {
                                    ImageVector.vectorResource(R.drawable.view2)
                                },
                                modifier = Modifier.size(20.dp),
                                contentDescription = if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                                    stringResource(R.string.show_as_list)
                                } else {
                                    stringResource(R.string.show_as_cards)
                                }
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
            if (!isSelectionMode) {
                GlassFab(
                    onClick = onAddRecipe,
                    modifier = Modifier.padding(bottom = FloatingBottomBarClearance)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_recipe)
                    )
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                BottomAppBar {
                    TextButton(onClick = { viewModel.selectAll() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.select),
                            modifier = Modifier.size(20.dp),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.select_all))
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleFavoriteSelected() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.favorite),
                            modifier = Modifier.size(20.dp),
                            contentDescription = stringResource(R.string.favorite)
                        )
                    }
                    IconButton(onClick = {
                        val ids = selectedIds.toList()
                        viewModel.clearSelection()
                        viewModel.requestDeleteBulk(ids)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = String.format(itemsDeletedCountTemplate, ids.size),
                                actionLabel = undoLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) viewModel.undoDeleteBulk(ids)
                        }
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.delete),
                            modifier = Modifier.size(20.dp),
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp
            )
        ) {
            item(key = "filters") {
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
                                    RecipeCategoryIcon(
                                        category = category,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (category == selectedCategory) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { viewModel.selectCategory(category); close() }
                            )
                        }
                    }

                    DropdownFilterChip(
                        displayText = selectedCookingMethod?.labelRes?.let { stringResource(it) }
                            ?: stringResource(R.string.cooking_method_label),
                        isActive = selectedCookingMethod != null
                    ) { close ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_cooking_methods)) },
                            onClick = { viewModel.selectCookingMethod(null); close() }
                        )
                        availableCookingMethods.forEach { (method, count) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.category_with_count,
                                            stringResource(method.labelRes),
                                            count
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                trailingIcon = {
                                    if (method == selectedCookingMethod) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { viewModel.selectCookingMethod(method); close() }
                            )
                        }
                    }

                    DropdownFilterChip(
                        modifier = Modifier.widthIn(max = 230.dp),
                        displayText = stringResource(groupingOption.labelRes),
                        isActive = groupingOption != RecipeGroupingOption.CATEGORY
                    ) { close ->
                        RecipeGroupingOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.labelRes)) },
                                trailingIcon = {
                                    if (option == groupingOption) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { viewModel.selectGroupingOption(option); close() }
                            )
                        }
                    }

                    DropdownFilterChip(
                        modifier = Modifier.widthIn(max = 230.dp),
                        displayText = stringResource(sortOption.displayNameRes),
                        isActive = sortOption != RecipeSortOption.TITLE_ASC
                    ) { close ->
                        RecipeSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.displayNameRes)) },
                                trailingIcon = {
                                    if (option == sortOption) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { viewModel.selectSortOption(option); close() }
                            )
                        }
                    }
                }
            }

            if (isFiltering) {
                if (grouped.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.recipes),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.nothing_found),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else {
                    recipeGroupedItems(
                        grouped = grouped,
                        viewMode = viewMode,
                        isSelectionMode = isSelectionMode,
                        selectedIds = selectedIds,
                        onViewRecipe = onViewRecipe,
                        onEditRecipe = onEditRecipe,
                        onDelete = { requestDelete(it) },
                        onToggleFavorite = { viewModel.onToggleFavorite(it) },
                        onShare = onShare,
                        onEnterSelectionMode = { viewModel.enterSelectionMode(it) },
                        onToggleSelection = { viewModel.toggleSelection(it) }
                    )
                }
            } else {
                item(key = "category_carousel") {
                    RecipeCategoryCarousel(
                        onCategoryClick = onCategoryClick,
                        onShowAllClick = onShowAllCategories
                    )
                }
                item(key = "suggested_carousel") {
                    RecipeCarouselSection(
                        title = stringResource(R.string.can_cook_short),
                        recipes = suggested,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllSuggested
                    )
                }
                item(key = "all_recipes_carousel") {
                    RecipeCarouselSection(
                        title = stringResource(R.string.all_recipes),
                        recipes = allRecipes,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllRecipes
                    )
                }

                if (allRecipes.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.recipes),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.no_recipes_placeholder),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}