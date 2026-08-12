package com.shvarsman.coolinar.presentation.screens.recipe.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.domain.model.StepContentItem
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.presentation.screens.common.AppBottomSheet
import com.shvarsman.coolinar.presentation.screens.common.DurationPickerDialog
import com.shvarsman.coolinar.presentation.screens.common.DurationSelectorField
import com.shvarsman.coolinar.presentation.screens.common.FieldLabel
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.IngredientListCard
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.ProductPickerDialog
import com.shvarsman.coolinar.presentation.screens.common.QuantityUnitField
import com.shvarsman.coolinar.presentation.screens.common.SelectionTile
import com.shvarsman.coolinar.presentation.screens.common.SelectorField
import com.shvarsman.coolinar.presentation.screens.common.StepContent
import com.shvarsman.coolinar.presentation.screens.common.TimerMinutesPickerDialog
import com.shvarsman.coolinar.presentation.screens.common.TopBarSearchField
import com.shvarsman.coolinar.presentation.screens.common.buildRenderedSteps
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.icons.CookingMethodIcon
import com.shvarsman.coolinar.presentation.ui.icons.RecipeCategoryIcon
import com.shvarsman.coolinar.presentation.ui.icons.icon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.appSegmentedButtonColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    recipeId: String,
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
    var showTimerPickerDialog by remember { mutableStateOf(false) }
    var editingTimerIndex by remember { mutableStateOf<Int?>(null) }
    var editingTimerMinutes by remember { mutableStateOf(5) }
    var editingIngredient by remember { mutableStateOf<RecipeIngredient?>(null) }

    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onCoverPhotoSelected(it) } }

    val stepPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addStepImage(it) } }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    BackHandler {
        if (state.isDirty) showExitConfirmation = true else onDone()
    }

    val listState = rememberLazyListState()

    val renderedSteps = remember(state.steps) { buildRenderedSteps(state.steps) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.recipeId == "") {
                            stringResource(R.string.new_recipe)
                        } else {
                            stringResource(R.string.edit_recipe_title)
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    GlassIconButton(
                        onClick = {
                            if (state.isDirty) showExitConfirmation = true else onDone()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            StepsBottomAppBar(
                onAddPhoto = { stepPhotoPicker.launch("image/*") },
                onAddTimer = {
                    editingTimerIndex = null
                    editingTimerMinutes = 5
                    showTimerPickerDialog = true
                },
                onAddStep = { viewModel.addTextStep() }
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
                item {
                    CoverPhotoPicker(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        photoUri = state.photoUri,
                        onPick = { coverPhotoPicker.launch("image/*") }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    LabeledTextField(
                        label = stringResource(R.string.recipe_title_label),
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        placeholder = stringResource(R.string.recipe_title_label),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                item {
                    SelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = stringResource(R.string.recipe_category_label),
                        value = stringResource(state.category.labelRes),
                        placeholder = stringResource(R.string.select_category_placeholder),
                        leadingIcon = state.category.icon,
                        customLeadingIcon = { RecipeCategoryIcon(category = state.category, modifier = Modifier.size(20.dp)) },
                        onClick = { showCategoryBottomSheet = true },
                    )
                }

                item {
                    val currentMethod = state.cookingMethod
                    SelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = stringResource(R.string.cooking_method),
                        value = currentMethod?.labelRes?.let { stringResource(it) } ?: "",
                        placeholder = stringResource(R.string.select_method_placeholder),
                        leadingIcon = Icons.Filled.Kitchen,
                        customLeadingIcon = if (currentMethod != null) {
                            { CookingMethodIcon(method = currentMethod, modifier = Modifier.size(20.dp)) }
                        } else null,
                        onClick = { showCookingMethodBottomSheet = true }
                    )
                }

                item {
                    DurationSelectorField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        label = stringResource(R.string.duration_picker_title),
                        hours = state.cookingHours,
                        minutes = state.cookingMinutes,
                        leadingIcon = ImageVector.vectorResource(R.drawable.time),
                        onClick = { showDurationPickerDialog = true },
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        FieldLabel(stringResource(R.string.difficulty_label))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            RecipeDifficulty.entries.forEachIndexed { index, difficulty ->
                                SegmentedButton(
                                    selected = state.difficulty == difficulty,
                                    onClick = { viewModel.onDifficultyChange(difficulty) },
                                    colors = appSegmentedButtonColors(),
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = RecipeDifficulty.entries.size
                                    )
                                ) {
                                    Text(stringResource(difficulty.labelRes))
                                }
                            }
                        }
                    }
                }

                item {
                    LabeledTextField(
                        label = stringResource(R.string.description_label),
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = stringResource(R.string.recipe_description_placeholder),
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.ingredients),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { viewModel.openIngredientPicker() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.add))
                        }
                    }
                }
                if (state.ingredients.isNotEmpty()) {
                    item {
                        IngredientListCard(
                            ingredients = state.ingredients,
                            availabilityFor = { it.availability(fridgeItems) },
                            onRemove = { viewModel.removeIngredient(it) },
                            onIngredientClick = { editingIngredient = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    StepsSectionTitle()
                }

                StepContent(
                    renderedSteps = renderedSteps,
                    focusRequestIndex = focusRequestIndex,
                    onDeleteImageClick = { index -> viewModel.deleteStepItem(index) },
                    onTextChange = { index, text -> viewModel.onStepTextChange(index, text) },
                    onNext = { index -> viewModel.onStepNext(index) },
                    onFocusConsumed = { viewModel.clearFocusRequest() },
                    onTimerClick = { index ->
                        val timer = state.steps.getOrNull(index) as? StepContentItem.Timer
                        editingTimerIndex = index
                        editingTimerMinutes = timer?.minutes ?: 5
                        showTimerPickerDialog = true
                    },
                    onDeleteTimerClick = { index -> viewModel.deleteStepItem(index) }
                )
            }
        }
    }

    if (isIngredientPickerOpen) {
        ProductPickerDialog(
            catalog = fridgeProducts,
            onDismiss = { viewModel.closeIngredientPicker() },
            onConfirm = { product, unit, qty, _ -> viewModel.addIngredient(product, unit, qty) },
            onCreateProduct = { name, category, unit, isToTaste, isAlwaysAvailable ->
                viewModel.createProduct(name, category, unit, isToTaste, isAlwaysAvailable)
            }
        )
    }

    editingIngredient?.let { ingredient ->
        EditIngredientDialog(
            ingredient = ingredient,
            onDismiss = { editingIngredient = null },
            onConfirm = { unit, qty ->
                viewModel.updateIngredient(ingredient, unit, qty)
                editingIngredient = null
            }
        )
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text(stringResource(R.string.leave_editing_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                    onDone()
                }) { Text(stringResource(R.string.exit)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showCategoryBottomSheet) {
        AppBottomSheet(
            title = stringResource(R.string.select_category_placeholder),
            onDismissRequest = { showCategoryBottomSheet = false }
        ) { onClose ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(RecipeCategory.entries) { category ->
                    SelectionTile(
                        text = stringResource(category.labelRes),
                        icon = { RecipeCategoryIcon(category = category) },
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
        val filteredMethods = CookingMethod.entries.filter { method ->
            stringResource(method.labelRes).contains(searchQuery, ignoreCase = true)
        }

        AppBottomSheet(
            title = stringResource(R.string.cooking_method),
            fillMaxHeight = true,
            onDismissRequest = { showCookingMethodBottomSheet = false }
        ) { onClose ->
            TopBarSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = stringResource(R.string.search_method_placeholder),
                modifier = Modifier.fillMaxWidth()
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
                            text = stringResource(R.string.nothing_found),
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
                                text = stringResource(method.labelRes),
                                icon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        CookingMethodIcon(method = method)
                                    }
                                },
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

    if (showDurationPickerDialog) {
        DurationPickerDialog(
            initialHours = state.cookingHours,
            initialMinutes = state.cookingMinutes,
            onDismissRequest = { showDurationPickerDialog = false },
            onConfirm = { hours, minutes ->
                viewModel.onCookingTimeChange(hours, minutes)
                showDurationPickerDialog = false
            }
        )
    }

    if (showTimerPickerDialog) {
        TimerMinutesPickerDialog(
            initialMinutes = editingTimerMinutes,
            onDismissRequest = { showTimerPickerDialog = false },
            onConfirm = { minutes ->
                val index = editingTimerIndex
                if (index != null) {
                    viewModel.updateStepTimer(index, minutes)
                } else {
                    viewModel.addStepTimer(minutes)
                }
                showTimerPickerDialog = false
            }
        )
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(message) }
        )
    }
}

@Composable
private fun StepsSectionTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.cooking_steps),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepsBottomAppBar(
    onAddPhoto: () -> Unit,
    onAddTimer: () -> Unit,
    onAddStep: () -> Unit
) {
    BottomAppBar(
        actions = {
            ToolbarTooltipIconButton(
                icon = ImageVector.vectorResource(R.drawable.add_photo),
                label = stringResource(R.string.add_step_photo),
                onClick = onAddPhoto
            )
            ToolbarTooltipIconButton(
                icon = ImageVector.vectorResource(R.drawable.timer),
                label = stringResource(R.string.add_step_timer),
                onClick = onAddTimer
            )
        },
        floatingActionButton = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text(stringResource(R.string.add_step)) } },
                state = rememberTooltipState()
            ) {
                FloatingActionButton(
                    onClick = onAddStep,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_step)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolbarTooltipIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(24.dp),
                contentDescription = label
            )
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
            .clip(CornerShape),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = rememberSizedImageRequest(photoUri, 400.dp, 180.dp),
                    contentDescription = stringResource(R.string.recipe_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.add_photo),
                        modifier = Modifier.size(20.dp),
                        contentDescription = null
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.add_cover_photo))
                }
            }
        }
    }
}

@Composable
private fun EditIngredientDialog(
    ingredient: RecipeIngredient,
    onDismiss: () -> Unit,
    onConfirm: (unit: MeasureUnit, quantity: Double) -> Unit
) {
    var quantityText by remember { mutableStateOf(formatQty(ingredient.quantity)) }
    var selectedUnit by remember { mutableStateOf(ingredient.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ingredient.product.name) },
        text = {
            Column {
                FieldLabel(stringResource(R.string.ingredient_quantity_per_serving))
                QuantityUnitField(
                    quantityText = quantityText,
                    onQuantityChange = { quantityText = it },
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedUnit, quantityText.toDoubleOrNull() ?: ingredient.quantity)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel)) }
        }
    )
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()