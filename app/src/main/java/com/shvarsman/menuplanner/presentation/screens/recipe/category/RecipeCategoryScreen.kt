package com.shvarsman.menuplanner.presentation.screens.recipe.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.presentation.screens.common.CollapsingLargeTopAppBar
import com.shvarsman.menuplanner.presentation.screens.recipe.components.RecipeCard
import com.shvarsman.menuplanner.presentation.screens.recipe.components.RecipeListRow
import com.shvarsman.menuplanner.presentation.screens.recipe.list.RecipeViewMode
import com.shvarsman.menuplanner.presentation.utils.rememberOptimisticDelete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCategoryScreen(
    onBack: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeCategoryViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    var viewMode by rememberSaveable { mutableStateOf(RecipeViewMode.PHOTO_CARDS) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            CollapsingLargeTopAppBar(
                title = viewModel.category.displayName,
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
                            contentDescription = if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                                "Отображать списком"
                            } else {
                                "Отображать карточками"
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "В этой категории пока нет рецептов.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                items(recipes, key = { it.id }) { recipe ->
                    if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onViewRecipe(recipe.id) },
                            onEdit = { onEditRecipe(recipe.id) },
                            onDelete = { requestDelete(recipe) }
                        )
                    } else {
                        RecipeListRow(
                            recipe = recipe,
                            onClick = { onViewRecipe(recipe.id) },
                            onEdit = { onEditRecipe(recipe.id) },
                            onDelete = { requestDelete(recipe) }
                        )
                    }
                }
            }
        }
    }
}