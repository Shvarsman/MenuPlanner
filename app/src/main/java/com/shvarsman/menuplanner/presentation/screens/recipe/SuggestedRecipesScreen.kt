package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.presentation.screens.common.CollapsingLargeTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedRecipesScreen(
    onBack: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val grouped by viewModel.suggestedRecipesGrouped.collectAsStateWithLifecycle()
    var viewMode by rememberSaveable { mutableStateOf(RecipeViewMode.PHOTO_CARDS) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingLargeTopAppBar(
                title = "Можно приготовить",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
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
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (grouped.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.RestaurantMenu,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Пока нет рецептов, для которых хватает продуктов в холодильнике",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
            ) {
                recipeGroupedItems(
                    grouped = grouped,
                    viewMode = viewMode,
                    onViewRecipe = onViewRecipe,
                    onEditRecipe = onEditRecipe,
                    onDelete = { viewModel.onDelete(it) }
                )
            }
        }
    }
}