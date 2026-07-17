package com.shvarsman.menuplanner.presentation.screens.recipeeditor

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.screens.common.AppBottomSheet
import com.shvarsman.menuplanner.presentation.screens.common.DurationPickerDialog
import com.shvarsman.menuplanner.presentation.screens.common.DurationSelectorField
import com.shvarsman.menuplanner.presentation.screens.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.screens.common.SelectionTile
import com.shvarsman.menuplanner.presentation.screens.common.SelectorField
import com.shvarsman.menuplanner.presentation.screens.common.StepContent
import com.shvarsman.menuplanner.presentation.screens.common.buildRenderedSteps
import com.shvarsman.menuplanner.presentation.screens.common.formatCookingTime
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    recipeId: Long,
    onDone: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val fridgeProducts by viewModel.catalog.collectAsStateWithLifecycle()
    val fridgeItems by viewModel.fridgeItems.collectAsStateWithLifecycle()
    val focusRequestIndex by viewModel.focusRequestIndex.collectAsStateWithLifecycle()
    val isIngredientPickerOpen by viewModel.isIngredientPickerOpen.collectAsStateWithLifecycle()

    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showCookingMethodBottomSheet by remember { mutableStateOf(false) }

    var showExitConfirmation by remember { mutableStateOf(false) }

    var showDurationPickerDialog by remember { mutableStateOf(false) }

    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onCoverPhotoSelected(it) } }

    val stepPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addStepImage(it) } }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    BackHandler { showExitConfirmation = true }

    val listState = rememberLazyListState()

    val stepsHeaderIndex = remember(state.ingredients.size) {
        5 + state.ingredients.size
    }

    val renderedSteps = remember(state.steps) { buildRenderedSteps(state.steps) }

    val showPinnedStepsHeader by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            firstVisible > stepsHeaderIndex ||
                    (firstVisible == stepsHeaderIndex && listState.firstVisibleItemScrollOffset > 0)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.recipeId == 0L) "Новый рецепт" else "Редактировать рецепт",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmation = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                // ── Фото обложки ──────────────────────────────────────────
                item {
                    CoverPhotoPicker(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        photoUri = state.photoUri,
                        onPick = { coverPhotoPicker.launch("image/*") }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Название ──────────────────────────────────────────────
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

                item {
                    SelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = "Категория рецепта",
                        value = state.category.displayName,
                        placeholder = "Выберите категорию",
                        leadingIcon = state.category.icon,
                        onClick = { showCategoryBottomSheet = true },
                    )
                }

                item {
                    SelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = "Способ приготовления",
                        value = state.cookingMethod?.displayName ?: "",
                        placeholder = "Выберите способ",
                        leadingIcon = Icons.Filled.Kitchen,
                        onClick = { showCookingMethodBottomSheet = true }
                    )
                }

                item {
                    DurationSelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = "Время приготовления",
                        hours = state.cookingHours,
                        minutes = state.cookingMinutes,
                        leadingIcon = Icons.Outlined.Schedule,
                        onClick = { showDurationPickerDialog = true },
                    )
                }

                // ── Ингредиенты ───────────────────────────────────────────
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
                        fridgeItems = fridgeItems,
                        onRemove = { viewModel.removeIngredient(ingredient) }
                    )
                }

                // ── Оригинальный заголовок "Шаги приготовления" ──────────
                item {
                    StepsHeaderBar(
                        onAddPhoto = { stepPhotoPicker.launch("image/*") },
                        onAddStep = { viewModel.addTextStep() }
                    )
                }

                StepContent(
                    renderedSteps = renderedSteps,
                    focusRequestIndex = focusRequestIndex,
                    onDeleteImageClick = { index -> viewModel.deleteStepItem(index) },
                    onTextChange = { index, text -> viewModel.onStepTextChange(index, text) },
                    onNext = { index -> viewModel.onStepNext(index) },
                    onFocusConsumed = { viewModel.clearFocusRequest() }
                )
            }

            if (showPinnedStepsHeader) {
                StepsHeaderBar(
                    onAddPhoto = { stepPhotoPicker.launch("image/*") },
                    onAddStep = { viewModel.addTextStep() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    if (isIngredientPickerOpen) {
        ProductPickerDialog(
            catalog = fridgeProducts,
            onDismiss = { viewModel.closeIngredientPicker() },
            onConfirm = { product, unit, qty -> viewModel.addIngredient(product, unit, qty) },
            onCreateProduct = { name, category, unit ->
                viewModel.createProduct(
                    name,
                    category,
                    unit
                )
            }
        )
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Покинуть редактирование?") },
            text = { Text("Несохранённые изменения будут потеряны.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                    onDone()
                }) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text("Отмена") }
            }
        )
    }

    if (showCategoryBottomSheet) {
        AppBottomSheet(
            title = "Выберите категорию",
            onDismissRequest = { showCategoryBottomSheet = false }
        ) { onClose ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RecipeCategory.entries) { category ->
                    SelectionTile(
                        text = category.displayName,
                        icon = category.icon,
                        isSelected = state.category == category,
                        onClick = {
                            viewModel.onCategoryChange(category)
                            onClose()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCookingMethodBottomSheet) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredMethods = remember(searchQuery) {
            CookingMethod.entries.filter { method ->
                method.displayName.contains(searchQuery, ignoreCase = true)
            }
        }

        AppBottomSheet(
            title = "Способ приготовления",
            fillMaxHeight = true,
            onDismissRequest = { showCookingMethodBottomSheet = false }
        ) { onClose ->
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск способа...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(AppCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (filteredMethods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ничего не найдено",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredMethods) { method ->
                            val isSelected = state.cookingMethod == method
                            SelectionTile(
                                text = method.displayName,
                                icon = if (isSelected) Icons.Default.Check else Icons.Default.Kitchen,
                                isSelected = isSelected,
                                useTransparentUnselected = true,
                                minHeight = 56.dp,
                                onClick = {
                                    viewModel.onCookingMethodChange(method)
                                    onClose()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора времени приготовления
    if (showDurationPickerDialog) {
        DurationPickerDialog(
            initialHours = state.cookingHours,
            initialMinutes = state.cookingMinutes,
            onDismissRequest = { showDurationPickerDialog = false },
            onConfirm = { hours, minutes ->
                // Вызываем метод вашего ViewModel для сохранения времени
                viewModel.onCookingTimeChange(hours, minutes)
                showDurationPickerDialog = false
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
private fun StepsHeaderBar(
    onAddPhoto: () -> Unit,
    onAddStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Шаги приготовления", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAddPhoto) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Добавить фото к шагу")
                }
                IconButton(onClick = onAddStep) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Добавить шаг",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
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
            .clip(RoundedCornerShape(AppCornerRadius)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = rememberSizedImageRequest(photoUri, 400.dp, 180.dp),
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
private fun IngredientRow(
    ingredient: RecipeIngredient,
    fridgeItems: List<FridgeItem>,
    onRemove: () -> Unit
) {
    val status = ingredient.availability(fridgeItems)
    val textColor = when (status) {
        IngredientAvailability.AVAILABLE -> MaterialTheme.colorScheme.primary
        IngredientAvailability.INSUFFICIENT -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            if (ingredient.product.isToTaste) {
                "${ingredient.product.name} — по вкусу"
            } else {
                "${ingredient.product.name} — ${formatQty(ingredient.quantity)} ${ingredient.unit.displayName}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (ingredient.product.isToTaste) MaterialTheme.colorScheme.onSurfaceVariant else textColor
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Удалить ингредиент")
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()