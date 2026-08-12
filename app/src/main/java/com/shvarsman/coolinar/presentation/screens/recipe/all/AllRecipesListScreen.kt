package com.shvarsman.coolinar.presentation.screens.recipe.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.CollapsingLargeTopAppBar
import com.shvarsman.coolinar.presentation.screens.recipe.components.recipeGroupedItems
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeListViewModel
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeViewMode
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.rememberOptimisticDelete
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRecipesListScreen(
    onBack: () -> Unit,
    onViewRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val grouped by viewModel.allRecipesGrouped.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recipeDeletedTemplate = stringResource(R.string.recipe_deleted)
    val itemsDeletedCountTemplate = stringResource(R.string.items_deleted_count)

    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.undo)
    val requestDelete = rememberOptimisticDelete<RecipeSummary, String>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { recipe -> String.format(recipeDeletedTemplate, recipe.title) },
        undoLabel = undoLabel,
        onDelete = { id -> viewModel.requestDelete(id) },
        onUndo = { id -> viewModel.undoDelete(id) }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CollapsingLargeTopAppBar(
                title = if (isSelectionMode) {
                    stringResource(R.string.selected_count, selectedIds.size)
                } else {
                    stringResource(R.string.all_recipes)
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(CornerShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                CornerShape
                            )
                            .gradientStyle(shape = CornerShape),
                        onClick = { if (isSelectionMode) viewModel.clearSelection() else onBack() }
                    ) {
                        Icon(
                            imageVector = if (isSelectionMode) {
                                Icons.Filled.Close
                            } else {
                                Icons.AutoMirrored.Filled.ArrowBack
                            },
                            contentDescription = if (isSelectionMode) {
                                stringResource(R.string.close_selection)
                            } else {
                                stringResource(R.string.back)
                            }
                        )
                    }
                },
                actions = {
                    if (!isSelectionMode) {
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
                                viewModel.setViewMode(
                                    if (viewMode == RecipeViewMode.PHOTO_CARDS) RecipeViewMode.LIST
                                    else RecipeViewMode.PHOTO_CARDS
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                                    ImageVector.vectorResource(R.drawable.view1)
                                } else {
                                    ImageVector.vectorResource(R.drawable.view2)
                                },
                                modifier = Modifier.size(20.dp),
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isSelectionMode) {
                BottomAppBar {
                    TextButton(onClick = {
                        viewModel.selectAllVisible(grouped.flatMap { it.second }.map { it.id })
                    }) {
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
                            if (result == SnackbarResult.ActionPerformed) viewModel.undoDeleteBulk(
                                ids
                            )
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
        if (grouped.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_recipes_at_all))
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
                    isSelectionMode = isSelectionMode,
                    selectedIds = selectedIds,
                    onViewRecipe = onViewRecipe,
                    onEditRecipe = onEditRecipe,
                    onDelete = { requestDelete(it) },
                    onToggleFavorite = { viewModel.onToggleFavorite(it) },
                    onShare = { recipe ->
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(recipe.title)
                            .startChooser()
                    },
                    onEnterSelectionMode = { viewModel.enterSelectionMode(it) },
                    onToggleSelection = { viewModel.toggleSelection(it) }
                )
            }
        }
    }
}