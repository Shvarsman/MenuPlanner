package com.shvarsman.coolinar.presentation.screens.recipe.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.formatCookingTime

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

    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = CornerShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        ListItem(
            headlineContent = { Text(recipe.title, maxLines = 1) },
            supportingContent = {
                val timeText = recipe.cookingTimeMinutes?.let { formatCookingTime(it) }
                    ?: stringResource(R.string.duration_unknown)
                Text(
                    stringResource(
                        R.string.recipe_time_difficulty,
                        timeText,
                        stringResource(recipe.difficulty.labelRes)
                    )
                )
            },
            leadingContent = {
                if (isSelectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CornerShape)
                    ) {
                        if (recipe.photoUri != null) {
                            AsyncImage(
                                model = rememberSizedImageRequest(recipe.photoUri, 56.dp, 56.dp),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(56.dp)
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.recipes),
                                        modifier = Modifier.size(20.dp),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            },
            trailingContent = {
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
        )
    }
}