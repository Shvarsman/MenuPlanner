package com.shvarsman.coolinar.presentation.screens.recipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeGroupHeader
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeViewMode
import com.shvarsman.coolinar.presentation.ui.icons.RecipeCategoryIcon

/**
 * Сгруппированный список рецептов с заголовками и переключением вида карточек
 * (фото/список). Группировка теперь не только по категории (см.
 * RecipeGroupingOption во ViewModel), поэтому принимает уже готовый список
 * пар "заголовок группы — рецепты", а не Map<RecipeCategory, ...>.
 * Переиспользуется на трёх экранах: RecipeListScreen, SuggestedRecipesScreen,
 * AllRecipesListScreen.
 */
fun LazyListScope.recipeGroupedItems(
    grouped: List<Pair<RecipeGroupHeader, List<RecipeSummary>>>,
    viewMode: RecipeViewMode,
    isSelectionMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    onViewRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    onDelete: (RecipeSummary) -> Unit,
    onToggleFavorite: (RecipeSummary) -> Unit = {},
    onShare: (RecipeSummary) -> Unit = {},
    onEnterSelectionMode: (String) -> Unit = {},
    onToggleSelection: (String) -> Unit = {}
) {
    grouped.forEach { (header, groupRecipes) ->
        item(key = "header_${header.id}") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                header.category?.let { category ->
                    RecipeCategoryIcon(category = category, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = stringResource(header.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
        }
        items(groupRecipes, key = { it.id }) { recipe ->
            val isSelected = recipe.id in selectedIds
            if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                RecipeCard(
                    recipe = recipe,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelectionMode) onToggleSelection(recipe.id) else onViewRecipe(recipe.id)
                    },
                    onLongClick = { onEnterSelectionMode(recipe.id) },
                    onEdit = { onEditRecipe(recipe.id) },
                    onDelete = { onDelete(recipe) },
                    onToggleFavorite = { onToggleFavorite(recipe) },
                    onShare = { onShare(recipe) },
                    onSelect = { onEnterSelectionMode(recipe.id) }
                )
            } else {
                RecipeListRow(
                    recipe = recipe,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelectionMode) onToggleSelection(recipe.id) else onViewRecipe(recipe.id)
                    },
                    onLongClick = { onEnterSelectionMode(recipe.id) },
                    onEdit = { onEditRecipe(recipe.id) },
                    onDelete = { onDelete(recipe) },
                    onToggleFavorite = { onToggleFavorite(recipe) },
                    onShare = { onShare(recipe) },
                    onSelect = { onEnterSelectionMode(recipe.id) }
                )
            }
        }
    }
}