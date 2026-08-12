package com.shvarsman.coolinar.presentation.screens.recipe.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.CollapsingLargeTopAppBar
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCard
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeListRow
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeViewMode
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.rememberOptimisticDelete
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCategoryScreen(
    onBack: () -> Unit,
    onViewRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    viewModel: RecipeCategoryViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    var viewMode by rememberSaveable { mutableStateOf(RecipeViewMode.PHOTO_CARDS) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CollapsingLargeTopAppBar(
                title = if (isSelectionMode) {
                    stringResource(R.string.selected_count, selectedIds.size)
                } else {
                    stringResource(viewModel.category.labelRes)
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
                            imageVector = if (isSelectionMode) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
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
                }
            )
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
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.no_recipes_in_category),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
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
                    val isSelected = recipe.id in selectedIds
                    val onShare = {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(recipe.title)
                            .startChooser()
                    }
                    if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                        RecipeCard(
                            recipe = recipe,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(recipe.id) else onViewRecipe(
                                    recipe.id
                                )
                            },
                            onLongClick = { viewModel.enterSelectionMode(recipe.id) },
                            onEdit = { onEditRecipe(recipe.id) },
                            onDelete = { requestDelete(recipe) },
                            onToggleFavorite = { viewModel.onToggleFavorite(recipe) },
                            onShare = onShare,
                            onSelect = { viewModel.enterSelectionMode(recipe.id) }
                        )
                    } else {
                        RecipeListRow(
                            recipe = recipe,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(recipe.id) else onViewRecipe(
                                    recipe.id
                                )
                            },
                            onLongClick = { viewModel.enterSelectionMode(recipe.id) },
                            onEdit = { onEditRecipe(recipe.id) },
                            onDelete = { requestDelete(recipe) },
                            onToggleFavorite = { viewModel.onToggleFavorite(recipe) },
                            onShare = onShare,
                            onSelect = { viewModel.enterSelectionMode(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}