package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch

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

    var sortMenuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                Column {
                    TopAppBar(
                        title = { Text("Рецепты", fontSize = 24.sp, fontWeight = FontWeight.Medium) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    DockedSearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = localSearchQuery,
                                onQueryChange = onLocalSearchQueryChange,
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text("Поиск рецептов") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (localSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onLocalSearchQueryChange("") }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Очистить")
                                        }
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        content = {}
                    )

                    Box(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                        AssistChip(
                            onClick = { sortMenuExpanded = true },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                            },
                            label = { Text(sortOption.displayName) }
                        )
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            RecipeSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.selectSortOption(option)
                                        sortMenuExpanded = false
                                    },
                                    trailingIcon = {
                                        if (option == sortOption) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (availableCategories.isNotEmpty()) {
                        RecipeCategoryFilterChips(
                            categories = availableCategories,
                            selectedCategory = selectedCategory,
                            onCategoryClick = { viewModel.selectCategory(it) },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
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
                            Icons.Filled.MenuBook,
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
                        RecipeCard(
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

/**
 * Ряд чипов категорий для фильтрации плоского списка рецептов на этом же экране —
 * отдельно от карусели/грида категорий, которые ведут на другой экран. Изначально
 * показаны первые несколько категорий, остальные — за чипом "Ещё N", тем же
 * паттерном, что и в каталоге продуктов.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecipeCategoryFilterChips(
    categories: List<Pair<RecipeCategory, Int>>,
    selectedCategory: RecipeCategory?,
    onCategoryClick: (RecipeCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val collapsedCount = 5

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
            onClick = { onCategoryClick(null) },
            label = { Text("Все категории") }
        )

        visibleCategories.forEach { (category, count) ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategoryClick(category) },
                label = { Text("${category.displayName} ($count)") },
                leadingIcon = {
                    RecipeCategoryIcon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        category = category,

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
private fun CategoryHeader(category: RecipeCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RecipeCategoryIcon(
            modifier = Modifier.size(20.dp),
            category = category
        )
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}