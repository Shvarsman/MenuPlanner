package com.shvarsman.menuplanner.presentation.screens.recipe

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.screens.common.ProductPickerDialog
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.menuplanner.presentation.ui.icons.icon
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import kotlinx.coroutines.launch

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

    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    var showExitConfirmation by remember { mutableStateOf(false) }

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

    // Оптимизировано: убран лишний derivedStateOf, так как размер меняется редко
    val stepsHeaderIndex = remember(state.ingredients.size) {
        5 + state.ingredients.size
    }

    // ИСПРАВЛЕНО: Вычисляем сгруппированные шаги в Composable-контексте экрана
    val renderedSteps = remember(state.steps) { buildRenderedSteps(state.steps) }

    // true, когда пользователь проскроллил мимо оригинального заголовка "Шаги приготовления"
    val showPinnedStepsHeader by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            firstVisible > stepsHeaderIndex ||
                    (firstVisible == stepsHeaderIndex && listState.firstVisibleItemScrollOffset > 0)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Исправлено на dp
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
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            readOnly = true,
                            value = state.category.displayName,
                            onValueChange = {},
                            label = { Text("Категория рецепта") },
                            leadingIcon = {
                                Icon(
                                    state.category.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Открыть выбор категорий",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppCornerRadius)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { openBottomSheet = !openBottomSheet }
                        )
                    }
                }

                // ── Метод приготовления и время ──────────────────────────
                item {
                    var showCookingMethodPicker by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box {
                            OutlinedTextField(
                                value = state.cookingMethod?.displayName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Способ приготовления") },
                                placeholder = { Text("Выберите способ") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Kitchen,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppCornerRadius)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showCookingMethodPicker = true }
                            )
                        }

                        if (showCookingMethodPicker) {
                            CookingMethodPickerDialog(
                                current = state.cookingMethod,
                                onDismiss = { showCookingMethodPicker = false },
                                onSelect = { method ->
                                    viewModel.onCookingMethodChange(method)
                                    showCookingMethodPicker = false
                                }
                            )
                        }

                        Text("Время приготовления", style = MaterialTheme.typography.labelLarge)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompactNumberPicker(
                                value = state.cookingHours,
                                onValueChange = {
                                    viewModel.onCookingTimeChange(
                                        it,
                                        state.cookingMinutes
                                    )
                                },
                                range = 0..23,
                                label = "Часы",
                                modifier = Modifier.weight(1f)
                            )
                            CompactNumberPicker(
                                value = state.cookingMinutes,
                                onValueChange = {
                                    viewModel.onCookingTimeChange(
                                        state.cookingHours,
                                        it
                                    )
                                },
                                range = 0..59,
                                label = "Минуты",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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

    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = AppCornerRadius, topEnd = AppCornerRadius),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Выберите категорию",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(RecipeCategory.entries) { category ->
                        val isSelected = state.category == category

                        Surface(
                            onClick = {
                                viewModel.onCategoryChange(category)
                                scope
                                    .launch { bottomSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            openBottomSheet = false
                                        }
                                    }
                            },
                            shape = RoundedCornerShape(AppCornerRadius),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            tonalElevation = if (isSelected) 0.dp else 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = value.toString(),
            onValueChange = {},
            label = { Text(label) },
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(AppCornerRadius)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp),
            shape = RoundedCornerShape(AppCornerRadius)
        ) {
            range.forEach { number ->
                DropdownMenuItem(
                    text = { Text(number.toString()) },
                    onClick = {
                        onValueChange(number)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()