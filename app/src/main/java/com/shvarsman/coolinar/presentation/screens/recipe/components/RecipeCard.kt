package com.shvarsman.coolinar.presentation.screens.recipe.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeSummary
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import com.shvarsman.coolinar.presentation.utils.formatCookingTime

private val CardImageHeight = 160.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeCard(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = CornerShape
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardImageHeight)
        ) {
            if (recipe.photoUri != null) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = rememberSizedImageRequest(
                        model = recipe.photoUri,
                        width = maxWidth,
                        height = CardImageHeight
                    ),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(48.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.recipes),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1
                )
                val timeText = recipe.cookingTimeMinutes?.let { formatCookingTime(it) }
                    ?: stringResource(R.string.duration_unknown)
                Text(
                    text = stringResource(
                        R.string.recipe_time_difficulty,
                        timeText,
                        stringResource(recipe.difficulty.labelRes)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White,
                            checkmarkColor = Color.White
                        )
                    )
                } else {
                    if (recipe.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.favorite),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                modifier = Modifier.size(20.dp),
                                contentDescription = stringResource(R.string.actions),
                                tint = Color.White
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
}