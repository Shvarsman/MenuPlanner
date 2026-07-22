package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.RecipeSummary
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest

private const val CAROUSEL_PREVIEW_LIMIT = 15

/**
 * Карусель рецептов с заголовком секции и кнопкой "Показать все" — тот же
 * паттерн, что и у карусели категорий (RecipeCategoryCarousel), только элементы —
 * фото самих рецептов, а не иконки категорий. Показывает не больше
 * CAROUSEL_PREVIEW_LIMIT штук — полный список открывается отдельным экраном.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCarouselSection(
    title: String,
    recipes: List<RecipeSummary>,
    onRecipeClick: (Long) -> Unit,
    onShowAllClick: () -> Unit
) {
    if (recipes.isEmpty()) return
    val preview = recipes.take(CAROUSEL_PREVIEW_LIMIT)
    val carouselState = rememberCarouselState { preview.size }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onShowAllClick) {
                Text("Показать все")
            }
        }

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = 4.dp),
            preferredItemWidth = 160.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { index ->
            val recipe = preview[index]
            RecipeCarouselTile(
                recipe = recipe,
                modifier = Modifier
                    .height(140.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge),
                onClick = { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
private fun RecipeCarouselTile(
    recipe: RecipeSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        if (recipe.photoUri != null) {
            AsyncImage(
                model = rememberSizedImageRequest(recipe.photoUri, 160.dp, 140.dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                    )
                )
        )

        Text(
            text = recipe.title,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        )
    }
}