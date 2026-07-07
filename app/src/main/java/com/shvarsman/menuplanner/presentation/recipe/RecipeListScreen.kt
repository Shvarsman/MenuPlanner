package com.shvarsman.menuplanner.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onViewRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рецепты", fontSize = 24.sp, fontWeight = FontWeight.Medium) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить рецепт")
            }
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Рецептов пока нет.\nДобавьте свой первый рецепт.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                    start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onViewRecipe(recipe.id) },
                        onEdit = { onEditRecipe(recipe.id) },
                        onDelete = { viewModel.onDelete(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (recipe.photoUri != null) {
                AsyncImage(
                    model = recipe.photoUri,
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.MenuBook, contentDescription = null)
                        }
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${recipe.ingredients.size} ингредиентов · ${recipe.steps.size} шагов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Редактировать рецепт")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить рецепт")
            }
        }
    }
}