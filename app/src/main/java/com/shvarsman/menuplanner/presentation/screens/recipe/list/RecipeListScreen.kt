package com.shvarsman.menuplanner.presentation.screens.recipe.list

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.presentation.screens.common.DropdownFilterChip
import com.shvarsman.menuplanner.presentation.screens.common.GlassFab
import com.shvarsman.menuplanner.presentation.screens.common.TopBarSearchField
import com.shvarsman.menuplanner.presentation.screens.recipe.components.RecipeCarouselSection
import com.shvarsman.menuplanner.presentation.screens.recipe.components.RecipeCategoryCarousel
import com.shvarsman.menuplanner.presentation.screens.recipe.components.recipeGroupedItems
import com.shvarsman.menuplanner.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.menuplanner.presentation.ui.theme.CornerShape
import com.shvarsman.menuplanner.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.menuplanner.presentation.ui.theme.gradientStyle
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch
import com.shvarsman.menuplanner.presentation.utils.rememberOptimisticDelete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
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

    val snackbarHostState = remember { SnackbarHostState() }
    val requestDelete = rememberOptimisticDelete<RecipeSummary, Long>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { recipe -> "«${recipe.title}» удалён" },
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
                        placeholder = "Поиск рецептов"
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
                                    "Отображать списком"
                                } else {
                                    "Отображать карточками"
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
                Icon(Icons.Filled.Add, contentDescription = "Добавить продукт")
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
                        displayText = selectedCategory?.displayName ?: "Категория",
                        isActive = selectedCategory != null
                    ) { close ->
                        DropdownMenuItem(
                            text = { Text("Все категории") },
                            onClick = { viewModel.selectCategory(null); close() }
                        )
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
                                "Ничего не найдено",
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
                        title = "Можно приготовить",
                        recipes = suggested,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllSuggested
                    )
                }
                item(key = "all_recipes_carousel") {
                    RecipeCarouselSection(
                        title = "Все рецепты",
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
                                "Рецептов пока нет.\nДобавьте свой первый рецепт.",
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