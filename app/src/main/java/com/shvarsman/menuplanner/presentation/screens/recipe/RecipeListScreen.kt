package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    onCategoryClick: (RecipeCategory) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val recipes by viewModel.filteredRecipes.collectAsStateWithLifecycle()
    val grouped by viewModel.groupedRecipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching = searchQuery.isNotBlank()
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(searchQuery) {
        viewModel.onSearchQueryChange(it)
    }

    val listState = rememberLazyListState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column {
                    TopAppBar(
                        title = { Text("Рецепты", fontSize = 24.sp, fontWeight = FontWeight.Medium) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = onLocalSearchQueryChange,
                        placeholder = { Text("Поиск рецептов") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (localSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onLocalSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Очистить")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(AppCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
            if (!isSearching) {
                item(key = "category_grid") {
                    RecipeCategoryGrid(onCategoryClick = onCategoryClick)
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
                            if (isSearching) "Ничего не найдено" else "Рецептов пока нет.\nДобавьте свой первый рецепт.",
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

@Composable
private fun CategoryHeader(category: RecipeCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
