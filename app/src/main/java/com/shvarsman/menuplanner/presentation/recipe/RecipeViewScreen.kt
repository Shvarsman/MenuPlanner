package com.shvarsman.menuplanner.presentation.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.cooking.CookingStepsReadOnly
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeViewScreen(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: RecipeViewViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    val state by viewModel.state.collectAsState()
    val fridgeItems by viewModel.fridgeItems.collectAsState()
    val shareState by viewModel.shareState.collectAsState()

    // Множитель порций — только для отображения на этом экране, в рецепт не сохраняется.
    // Ингредиенты в БД всегда хранятся из расчёта на 1 порцию.
    var servings by remember(recipeId) { mutableStateOf(1) }

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let { viewModel.onShare(recipeId, it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.recipe?.title ?: "Рецепт",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val safeTitle = state.recipe?.title
                                ?.replace(Regex("[^a-zA-Zа-яА-Я0-9 ]"), "")
                                ?.take(40)
                                ?.ifBlank { "recipe" } ?: "recipe"
                            val timestamp = SimpleDateFormat(
                                "yyyy-MM-dd_HHmm",
                                Locale.getDefault()
                            ).format(Date())
                            shareLauncher.launch("${safeTitle}_$timestamp.zip")
                        },
                        enabled = shareState !is RecipeShareState.InProgress
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Поделиться рецептом")
                    }
                    IconButton(onClick = { onEdit(recipeId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val recipe = state.recipe
        if (recipe == null) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Рецепт не найден")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                if (recipe.photoUri != null) {
                    AsyncImage(
                        model = recipe.photoUri,
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                if (recipe.cookingMethod != null || recipe.cookingTimeMinutes != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recipe.cookingMethod?.let { method ->
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Kitchen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text(method.displayName) }
                            )
                        }
                        recipe.cookingTimeMinutes?.let { minutes ->
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text(formatCookingTime(minutes)) }
                            )
                        }
                    }
                }
            }

            // ── Ингредиенты + выбор порций ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                    ServingsStepper(
                        servings = servings,
                        onDecrease = { servings = (servings - 1).coerceAtLeast(1) },
                        onIncrease = { servings = (servings + 1).coerceAtMost(50) }
                    )
                }
            }
            items(recipe.ingredients) { ingredient ->
                IngredientViewRow(
                    ingredient = ingredient,
                    fridgeItems = fridgeItems,
                    servings = servings
                )
            }

            item {
                Text(
                    "Шаги приготовления",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            CookingStepsReadOnly(steps = recipe.steps)
        }
    }

    when (val share = shareState) {
        is RecipeShareState.InProgress -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Подождите") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Готовим файл рецепта...")
                    }
                }
            )
        }

        is RecipeShareState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearShareState() },
                confirmButton = { TextButton(onClick = { viewModel.clearShareState() }) { Text("Ок") } },
                title = { Text("Готово") },
                text = { Text("Рецепт сохранён в файл — можно переслать его другому пользователю приложения.") }
            )
        }

        is RecipeShareState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearShareState() },
                confirmButton = { TextButton(onClick = { viewModel.clearShareState() }) { Text("Ок") } },
                title = { Text("Ошибка") },
                text = { Text(share.message) }
            )
        }

        RecipeShareState.Idle -> {}
    }
}

/** Компактный степпер +/- количества порций. */
@Composable
private fun ServingsStepper(
    servings: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Уменьшить количество порций",
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            "$servings ${servingsLabel(servings)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.widthIn(min = 56.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Увеличить количество порций",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun servingsLabel(servings: Int): String {
    val lastTwoDigits = servings % 100
    val lastDigit = servings % 10
    return when {
        lastTwoDigits in 11..14 -> "порций"
        lastDigit == 1 -> "порция"
        lastDigit in 2..4 -> "порции"
        else -> "порций"
    }
}

@Composable
private fun IngredientViewRow(
    ingredient: RecipeIngredient,
    fridgeItems: List<FridgeItem>,
    servings: Int
) {
    if (ingredient.product.isToTaste) {
        Text(
            "${ingredient.product.name} — по вкусу",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        return
    }

    // Ингредиенты в БД хранятся из расчёта на 1 порцию — здесь только
    // визуальный пересчёт под выбранное количество порций, без изменения данных рецепта.
    val scaledQuantity = ingredient.quantity * servings
    val scaledIngredient = ingredient.copy(quantity = scaledQuantity)

    val status = scaledIngredient.availability(fridgeItems)
    val color = when (status) {
        IngredientAvailability.AVAILABLE -> MaterialTheme.colorScheme.primary
        IngredientAvailability.INSUFFICIENT -> MaterialTheme.colorScheme.error
    }
    Text(
        "${ingredient.product.name} — ${formatQty(scaledQuantity)} ${ingredient.unit.displayName}",
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    )
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

private fun formatCookingTime(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
        hours > 0 -> "$hours ч"
        else -> "$minutes мин"
    }
}