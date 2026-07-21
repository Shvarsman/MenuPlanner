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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.presentation.ui.icons.CategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.menuplanner.presentation.ui.icons.icon

/**
 * Карусель категорий рецептов (M3 HorizontalMultiBrowseCarousel) + кнопка
 * "Показать все" — открывает отдельный экран AllCategoriesScreen с полным
 * гридом, тем же способом, каким открывается экран одной категории.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCategoryCarousel(
    onCategoryClick: (RecipeCategory) -> Unit,
    onShowAllClick: () -> Unit
) {
    val categories = RecipeCategory.entries
    val carouselState = rememberCarouselState { categories.size }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Категории", style = MaterialTheme.typography.titleMedium)
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
            val category = categories[index]
            CategoryTile(
                category = category,
                modifier = Modifier
                    .height(140.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge),
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

/**
 * Общий тайл категории — используется и в карусели (с maskClip), и на экране
 * "Все категории" (с обычным clip + aspectRatio). internal — переиспользуется
 * из AllCategoriesScreen.kt в этом же пакете.
 */
@Composable
internal fun CategoryTile(
    category: RecipeCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            RecipeCategoryIcon(
                modifier = Modifier
                    .padding(32.dp)
                    .size(56.dp),
                category = category
            )
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
            text = category.displayName,
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