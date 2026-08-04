package com.shvarsman.coolinar.presentation.screens.recipe.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.presentation.screens.common.CollapsingLargeTopAppBar
import com.shvarsman.coolinar.presentation.screens.recipe.components.CategoryTile
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoriesScreen(
    onBack: () -> Unit,
    onCategoryClick: (RecipeCategory) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingLargeTopAppBar(
                title = "Все категории",
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
                        onClick = onBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(RecipeCategory.entries) { category ->
                CategoryTile(
                    category = category,
                    modifier = Modifier
                        .aspectRatio(1.3f)
                        .clip(CornerShape),
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}