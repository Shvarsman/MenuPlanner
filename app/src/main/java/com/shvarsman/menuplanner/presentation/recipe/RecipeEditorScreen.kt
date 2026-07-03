package com.shvarsman.menuplanner.presentation.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.shvarsman.menuplanner.domain.model.RecipeIngredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    recipeId: Long,
    onDone: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    val state by viewModel.state.collectAsState()
    val fridgeProducts by viewModel.fridgeProducts.collectAsState()

    var showFridgePicker by remember { mutableStateOf(false) }
    var showCustomIngredientDialog by remember { mutableStateOf(false) }
    var newStepText by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onPhotoSelected(it.toString()) } }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.recipeId == 0L) "Новый рецепт" else "Редактировать рецепт",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }) { Text("Сохранить") }
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

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PhotoPicker(photoUri = state.photoUri, onPick = { photoPicker.launch("image/*") })
            }

            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Название рецепта") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionHeader(
                    title = "Ингредиенты",
                    onAddFromFridge = { showFridgePicker = true },
                    onAddCustom = { showCustomIngredientDialog = true }
                )
            }
            items(state.ingredients) { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onRemove = { viewModel.removeIngredient(ingredient) })
            }

            item {
                Text("Шаги приготовления", style = MaterialTheme.typography.titleMedium)
            }
            itemsIndexed(state.steps) { index, step ->
                StepRow(
                    index = index,
                    text = step,
                    onTextChange = { viewModel.updateStepAt(index, it) },
                    onRemove = { viewModel.removeStepAt(index) }
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newStepText,
                        onValueChange = { newStepText = it },
                        label = { Text("Новый шаг") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.addStep(newStepText)
                        newStepText = ""
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Добавить шаг")
                    }
                }
            }
        }
    }

    if (showFridgePicker) {
        FridgeIngredientPickerDialog(
            products = fridgeProducts,
            onDismiss = { showFridgePicker = false },
            onConfirm = { product, qty ->
                viewModel.addIngredientFromFridge(product, qty)
                showFridgePicker = false
            }
        )
    }

    if (showCustomIngredientDialog) {
        CustomIngredientDialog(
            onDismiss = { showCustomIngredientDialog = false },
            onConfirm = { name, unit, qty ->
                viewModel.addCustomIngredient(name, unit, qty)
                showCustomIngredientDialog = false
            }
        )
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("Ок") } },
            title = { Text("Ошибка") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun PhotoPicker(photoUri: String?, onPick: () -> Unit) {
    Surface(
        onClick = onPick,
        modifier = Modifier
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
                    Text("Добавить фото")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAddFromFridge: () -> Unit, onAddCustom: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row {
            TextButton(onClick = onAddFromFridge) { Text("Из холодильника") }
            TextButton(onClick = onAddCustom) { Text("Свой") }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${ingredient.name} — ${formatQty(ingredient.quantity)} ${ingredient.unit.displayName}")
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Удалить ингредиент")
        }
    }
}

@Composable
private fun StepRow(
    index: Int,
    text: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("${index + 1}.", modifier = Modifier.padding(end = 8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Удалить шаг")
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
