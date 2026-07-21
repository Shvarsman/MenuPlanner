package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoriesScreen(
    onBack: () -> Unit,
    onCategoryClick: (RecipeCategory) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все категории", fontSize = 24.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                        .clip(RoundedCornerShape(AppCornerRadius)),
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}