package com.shvarsman.menuplanner.presentation.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.presentation.common.ProductPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    recipeId: Long,
    onDone: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    val isIngredientPickerOpen by viewModel.isIngredientPickerOpen.collectAsState()
    val catalog by viewModel.catalog.collectAsState()

    val state by viewModel.state.collectAsState()

    var showFridgePicker by remember { mutableStateOf(false) }
    var showCustomIngredientDialog by remember { mutableStateOf(false) }

    // Лаунчер для фото обложки рецепта
    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onCoverPhotoSelected(it) } }

    // Лаунчер для фото внутри шагов
    val stepPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addStepImage(it) } }

    val focusRequestIndex by viewModel.focusRequestIndex.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.recipeId == 0L) "Новый рецепт" else "Редактировать рецепт")
                },
                actions = {
                    // Кнопка добавления фото в шаги — прямо в топбаре как в NotesApp
                    IconButton(onClick = { stepPhotoPicker.launch("image/*") }) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = "Добавить фото к шагу")
                    }
                    TextButton(onClick = { viewModel.save() }) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            )
        ) {
            // ── Фото обложки ──────────────────────────────────────────────────
            item {
                CoverPhotoPicker(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    photoUri = state.photoUri,
                    onPick = { coverPhotoPicker.launch("image/*") }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Название ──────────────────────────────────────────────────────
            item {
                TextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    placeholder = { Text("Название рецепта") },
                    textStyle = MaterialTheme.typography.headlineSmall
                )
            }

            // ── Ингредиенты ───────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.openIngredientPicker() }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Добавить")
                    }
                }
            }

            items(state.ingredients) { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onRemove = { viewModel.removeIngredient(ingredient) })
            }

            // ── Шаги приготовления ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Шаги приготовления", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.addTextStep() }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Добавить шаг",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            StepContent(
                steps = state.steps,
                focusRequestIndex = focusRequestIndex,
                onDeleteImageClick = { index -> viewModel.deleteStepItem(index) },
                onTextChange = { index, text -> viewModel.onStepTextChange(index, text) },
                onNext = { index -> viewModel.onStepNext(index) },
                onFocusConsumed = { viewModel.clearFocusRequest() }
            )
        }
    }

    if (isIngredientPickerOpen) {
        ProductPickerDialog(
            catalog = catalog,
            onDismiss = { viewModel.closeIngredientPicker() },
            onConfirm = { product, unit, qty ->
                viewModel.addIngredient(product, unit, qty)
            },
            onCreateProduct = { name, category, unit ->
                viewModel.createProduct(
                    name,
                    category,
                    unit
                )
            }
        )
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("Ок") }
            },
            title = { Text("Ошибка") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun CoverPhotoPicker(
    modifier: Modifier = Modifier,
    photoUri: String?,
    onPick: () -> Unit
) {
    Surface(
        onClick = onPick,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото рецепта",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text("Добавить фото обложки")
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${ingredient.product.name} — ${formatQty(ingredient.quantity)} ${ingredient.unit.displayName}",
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Удалить ингредиент"
            )
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()