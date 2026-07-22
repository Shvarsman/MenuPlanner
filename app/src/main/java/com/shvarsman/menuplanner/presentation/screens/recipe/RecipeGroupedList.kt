package com.shvarsman.menuplanner.presentation.screens.recipe

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
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.presentation.ui.icons.RecipeCategoryIcon

/**
 * Сгруппированный по категориям список рецептов с заголовками и переключением
 * вида карточек (фото/список). Переиспользуется на трёх экранах: результаты
 * поиска/фильтра на RecipeListScreen, SuggestedRecipesScreen, AllRecipesListScreen —
 * чтобы не дублировать одну и ту же разметку трижды.
 */
fun LazyListScope.recipeGroupedItems(
    grouped: Map<RecipeCategory, List<RecipeSummary>>,
    viewMode: RecipeViewMode,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    onDelete: (RecipeSummary) -> Unit
) {
    grouped.forEach { (category, categoryRecipes) ->
        item(key = "header_${category.name}") {
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
        items(categoryRecipes, key = { it.id }) { recipe ->
            if (viewMode == RecipeViewMode.PHOTO_CARDS) {
                RecipeCard(
                    recipe = recipe,
                    onClick = { onViewRecipe(recipe.id) },
                    onEdit = { onEditRecipe(recipe.id) },
                    onDelete = { onDelete(recipe) }
                )
            } else {
                RecipeListRow(
                    recipe = recipe,
                    onClick = { onViewRecipe(recipe.id) },
                    onEdit = { onEditRecipe(recipe.id) },
                    onDelete = { onDelete(recipe) }
                )
            }
        }
    }
}