package com.shvarsman.menuplanner.presentation.recipe

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.RecipeIngredient
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.common.rememberSizedImageRequest
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

    // Индекс элемента-заголовка "Шаги приготовления" в LazyColumn.
    // Порядок фиксированный: обложка(1) + название(1) + категория(1) +
    // метод/время(1) + заголовок ингредиентов(1) + сами ингредиенты(N) = индекс заголовка шагов
    val stepsHeaderIndex by remember(state.ingredients.size) {
        derivedStateOf { 5 + state.ingredients.size }
    }

    // true, когда пользователь проскроллил мимо оригинального заголовка "Шаги приготовления"
    val showPinnedStepsHeader by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            firstVisible > stepsHeaderIndex ||
                    (firstVisible == stepsHeaderIndex && listState.firstVisibleItemScrollOffset > 0)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
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

                // ── Категория ─────────────────────────────────────────────
                item {
                    var categoryMenuExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = categoryMenuExpanded,
                            onExpandedChange = { categoryMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = state.category.displayName,
                                onValueChange = {},
                                label = { Text("Категория рецепта") },
                                leadingIcon = {
                                    Icon(
                                        state.category.icon,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ),
                                shape = RoundedCornerShape(AppCornerRadius)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryMenuExpanded,
                                onDismissRequest = { categoryMenuExpanded = false },
                                shape = RoundedCornerShape(AppCornerRadius)
                            ) {
                                RecipeCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.displayName) },
                                        leadingIcon = {
                                            Icon(
                                                category.icon,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            viewModel.onCategoryChange(category)
                                            categoryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                // Обычный элемент списка (не sticky) — прилипающая копия
                // рисуется отдельно поверх LazyColumn в Box ниже
                item {
                    StepsHeaderBar(
                        onAddPhoto = { stepPhotoPicker.launch("image/*") },
                        onAddStep = { viewModel.addTextStep() }
                    )
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

            // ── Прилипающая копия заголовка ──────────────────────────────
            // Показывается поверх списка только когда пользователь
            // проскроллил мимо оригинального заголовка внутри LazyColumn
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
        com.shvarsman.menuplanner.presentation.common.ProductPickerDialog(
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