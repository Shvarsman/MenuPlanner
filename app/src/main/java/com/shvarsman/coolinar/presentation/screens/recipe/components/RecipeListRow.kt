package com.shvarsman.coolinar.presentation.screens.recipe.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.formatCookingTime

/**
 * Компактная строка рецепта в стиле MenuEntryCard из WeekMenuScreen — прямоугольное
 * фото 4:3 слева, скруглённое только с внешней (левой) стороны, чтобы совпадать
 * с формой карточки, и заголовок + время/сложность с иконками справа. Второй вид
 * отображения списка рецептов, переключается тумблером в топ-баре RecipeListScreen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeListRow(
    recipe: RecipeSummary,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onSelect: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = CornerShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(4f / 3f)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(4f / 3f)
                ) {
                    if (recipe.photoUri != null) {
                        AsyncImage(
                            model = rememberSizedImageRequest(recipe.photoUri, 140.dp, 104.dp),
                            contentDescription = recipe.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CornerShape)
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CornerShape)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.recipes),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (recipe.isFavorite) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .padding(6.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.favorite),
                                contentDescription = stringResource(R.string.favorite),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.time),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val timeText = recipe.cookingTimeMinutes?.let { formatCookingTime(it) }
                        ?: stringResource(R.string.duration_unknown)
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.difficulty),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(recipe.difficulty.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.actions)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .clip(CornerShape)
                            .gradientStyle(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = CornerShape,
                        shadowElevation = 0.dp
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = { Text(stringResource(R.string.delete)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.delete),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                            },
                            onClick = { menuExpanded = false; onDelete() },
                            colors = MenuItemColors(
                                textColor = MaterialTheme.colorScheme.error,
                                leadingIconColor = MaterialTheme.colorScheme.error,
                                trailingIconColor = MaterialTheme.colorScheme.error,
                                disabledTextColor = MaterialTheme.colorScheme.error,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.error,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.error
                            )
                        )
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = { Text(stringResource(R.string.edit_action)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.edit),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                            },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = {
                                Text(
                                    if (recipe.isFavorite) stringResource(R.string.remove_from_favorites)
                                    else stringResource(R.string.add_to_favorites)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.favorite),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                            },
                            onClick = { menuExpanded = false; onToggleFavorite() }
                        )
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = { Text(stringResource(R.string.share)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.share),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                            },
                            onClick = { menuExpanded = false; onShare() }
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                        DropdownMenuItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = { Text(stringResource(R.string.select)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.select),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                            },
                            onClick = { menuExpanded = false; onSelect() }
                        )
                    }
                }
            }
        }
    }
}