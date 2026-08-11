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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isFiltering = searchQuery.isNotBlank() || selectedCategory != null
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }

    var viewMode by rememberSaveable { mutableStateOf(RecipeViewMode.PHOTO_CARDS) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val recipeDeletedTemplate = stringResource(R.string.recipe_deleted)

    val snackbarHostState = remember { SnackbarHostState() }
    val requestDelete = rememberOptimisticDelete<RecipeSummary, String>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { recipe -> String.format(recipeDeletedTemplate, recipe.title) },
        onRequestDelete = { id -> viewModel.requestDelete(id) },
        onUndo = { id -> viewModel.undoDelete(id) }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    TopBarSearchField(
                        modifier = Modifier.padding(end = 16.dp),
                        query = localSearchQuery,
                        onQueryChange = onLocalSearchQueryChange,
                        placeholder = stringResource(R.string.search_recipes)
                    )
                },
                actions = {
                    if (isFiltering) {
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
                                    Icons.AutoMirrored.Filled.ViewList
                                } else {
                                    Icons.Filled.GridView
                                },
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
                        displayText = stringResource(sortOption.displayNameRes),
                        isActive = sortOption != RecipeSortOption.TITLE_ASC
                    ) { close ->
                        RecipeSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.displayNameRes)) },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                                },
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
                                Icons.AutoMirrored.Filled.MenuBook,
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
                        onViewRecipe = onViewRecipe,
                        onEditRecipe = onEditRecipe,
                        onDelete = { requestDelete(it) }
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
                                Icons.AutoMirrored.Filled.MenuBook,
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