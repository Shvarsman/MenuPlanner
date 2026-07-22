package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius

/**
 * Компактная строка рецепта в стиле M3 "Lists" — квадратная миниатюра слева
 * (не на всю карточку, в отличие от RecipeCard), заголовок + подпись, действия
 * справа. Второй вид отображения списка рецептов, переключается тумблером
 * в топ-баре RecipeListScreen.
 */
@Composable
fun RecipeListRow(
    recipe: RecipeSummary,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppCornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        ListItem(
            headlineContent = { Text(recipe.title, maxLines = 1) },
            supportingContent = {
                Text("${recipe.ingredientCount} ингредиентов · ${recipe.stepCount} шагов")
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(AppCornerRadius))
                ) {
                    if (recipe.photoUri != null) {
                        AsyncImage(
                            model = rememberSizedImageRequest(recipe.photoUri, 56.dp, 56.dp),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                    }
                }
            }
        )
    }
}