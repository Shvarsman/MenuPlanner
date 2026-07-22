package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.presentation.screens.common.DropdownFilterChip
import com.shvarsman.menuplanner.presentation.screens.common.TopBarSearchField
import com.shvarsman.menuplanner.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch

private enum class RecipeViewMode { PHOTO_CARDS, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    onCategoryClick: (RecipeCategory) -> Unit,
    onShowAllCategories: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val recipes by viewModel.filteredRecipes.collectAsStateWithLifecycle()
    val grouped by viewModel.groupedRecipes.collectAsStateWithLifecycle()
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    TopBarSearchField(
                        query = localSearchQuery,
                        onQueryChange = onLocalSearchQueryChange,
                        placeholder = "Поиск рецептов"
                    )
                },
                actions = {
                    IconButton(
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
                                "Отображать списком"
                            } else {
                                "Отображать карточками"
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
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить рецепт")
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
            // Ряд фильтров теперь обычный элемент списка — прокручивается вместе
            // с контентом и уезжает вслед за топбаром, а не живёт отдельно от него
            item(key = "filters") {
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
                            text = { Text("Все категории") },
                            onClick = { viewModel.selectCategory(null); close() },

                        )
                        if (availableCategories.isNotEmpty()) {
                            HorizontalDivider()
                            availableCategories.forEach { (category, count) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${category.displayName} ($count)",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = {
                                        RecipeCategoryIcon(category = category, modifier = Modifier.size(24.dp))
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
                    }

                    DropdownFilterChip(
                        displayText = sortOption.displayName,
                        isActive = sortOption != RecipeSortOption.TITLE_ASC
                    ) { close ->
                        RecipeSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
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

            if (!isFiltering) {
                item(key = "category_grid") {
                    RecipeCategoryCarousel(
                        onCategoryClick = onCategoryClick,
                        onShowAllClick = onShowAllCategories
                    )
                }
                item(key = "all_recipes_header") {
                    Text(
                        "Все рецепты",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (recipes.isEmpty()) {
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
                            if (isFiltering) "Ничего не найдено" else "Рецептов пока нет.\nДобавьте свой первый рецепт.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                grouped.forEach { (category, categoryRecipes) ->
                    item(key = "header_${category.name}") { CategoryHeader(category) }
                    items(categoryRecipes, key = { it.id }) { recipe ->
                        if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onViewRecipe(recipe.id) },
                                onEdit = { onEditRecipe(recipe.id) },
                                onDelete = { viewModel.onDelete(recipe) }
                            )
                        } else {
                            RecipeListRow(
                                recipe = recipe,
                                onClick = { onViewRecipe(recipe.id) },
                                onEdit = { onEditRecipe(recipe.id) },
                                onDelete = { viewModel.onDelete(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: RecipeCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RecipeCategoryIcon(category = category, modifier = Modifier.size(20.dp))
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}