package com.shvarsman.coolinar.presentation.screens.recipe.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.RecipeIngredient
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.presentation.screens.common.AppBottomSheet
import com.shvarsman.coolinar.presentation.screens.common.FieldLabel
import com.shvarsman.coolinar.presentation.screens.common.QuantityUnitField
import com.shvarsman.coolinar.presentation.screens.common.ReadOnlyField
import com.shvarsman.coolinar.presentation.screens.common.localizedName
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.screens.cooking.CookingStepsReadOnly
import com.shvarsman.coolinar.presentation.ui.icons.CookingMethodIcon
import com.shvarsman.coolinar.presentation.ui.icons.ProductIcon
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val PhotoHeight = 320.dp
private val ContentOverlap = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeViewScreen(
    recipeId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: RecipeViewViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val fridgeItems by viewModel.fridgeItems.collectAsStateWithLifecycle()
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()
    val isAddToMenuSheetOpen by viewModel.isAddToMenuSheetOpen.collectAsStateWithLifecycle()
    val menuAddedEvent by viewModel.menuAddedEvent.collectAsStateWithLifecycle()

    var servings by remember(recipeId) { mutableIntStateOf(1) }
    var editingIngredient by remember { mutableStateOf<RecipeIngredient?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(menuAddedEvent) {
        if (menuAddedEvent > 0) {
            snackbarHostState.showSnackbar("Рецепт добавлен в меню")
        }
    }

    val requestDeleteIngredient: (RecipeIngredient) -> Unit = { ingredient ->
        viewModel.requestDeleteIngredient(ingredient.id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "«${ingredient.product.name}» удалён из рецепта",
                actionLabel = "Отменить",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDeleteIngredient(ingredient.id)
        }
    }

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let { viewModel.onShare(recipeId, it) } }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val recipe = state.recipe
        if (recipe == null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Рецепт не найден") }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { 3 })
        val tabs = listOf("Описание", "Ингредиенты", "Приготовление")
        val density = LocalDensity.current


        val maxOffsetPx = with(density) { (PhotoHeight - ContentOverlap).toPx() }
        val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val minOffsetPx = with(density) { (statusBarHeightDp + 56.dp).toPx() }

        val minOffsetDp = with(density) { minOffsetPx.toDp() }
        val navBarHeightDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val listBottomPadding = minOffsetDp + navBarHeightDp + 16.dp

        var offsetPx by remember { mutableFloatStateOf(maxOffsetPx) }

        val collapseConnection = remember(minOffsetPx, maxOffsetPx) {
            object : NestedScrollConnection {
                // 1. Перехватываем жест ДО внутреннего списка
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    // Палец движется вверх (скроллим контент вниз)
                    return if (delta < 0) {
                        val newOffset = (offsetPx + delta).coerceIn(minOffsetPx, maxOffsetPx)
                        val consumedByHeader = newOffset - offsetPx
                        offsetPx = newOffset
                        // Сообщаем Compose, сколько пикселей забрала шапка
                        Offset(0f, consumedByHeader)
                    } else {
                        Offset.Zero
                    }
                }

                // 2. Дорабатываем жест ПОСЛЕ того, как внутренний список уперся в край
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val delta = available.y
                    // Палец движется вниз (скроллим контент наверх), и список уже дошел до первой позиции (верх)
                    return if (delta > 0) {
                        val newOffset = (offsetPx + delta).coerceIn(minOffsetPx, maxOffsetPx)
                        val consumedByHeader = newOffset - offsetPx
                        offsetPx = newOffset
                        // Забираем оставшийся скролл на раскрытие шапки
                        Offset(0f, consumedByHeader)
                    } else {
                        Offset.Zero
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(collapseConnection)
        ) {
            if (recipe.photoUri != null) {
                AsyncImage(
                    model = rememberSizedImageRequest(recipe.photoUri, 480.dp, PhotoHeight),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PhotoHeight)
                        .align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PhotoHeight)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PhotoHeight)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                            endY = with(LocalDensity.current) { 140.dp.toPx() }
                        )
                    )
            )

            Surface(
                shape = RoundedCornerShape(topStart = ContentOverlap, topEnd = ContentOverlap),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, offsetPx.roundToInt()) }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 12.dp,
                            end = 16.dp
                        )
                    )

                    // Кнопка внутри "шапки" контейнера — при полном сворачивании
                    // контейнер доезжает до зоны навигационных иконок, и кнопка
                    // оказывается прямо под ними
                    Button(
                        onClick = { viewModel.openAddToMenuSheet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить в меню")
                    }

                    Spacer(Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = {
                                    Text(
                                        title,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> DescriptionPage(
                                recipe = recipe,
                                bottomPadding = listBottomPadding
                            )

                            1 -> IngredientsPage(
                                ingredients = recipe.ingredients,
                                fridgeItems = fridgeItems,
                                servings = servings,
                                onServingsChange = { servings = it },
                                onIngredientClick = { editingIngredient = it },
                                onIngredientDelete = requestDeleteIngredient,
                                bottomPadding = listBottomPadding
                            )

                            2 -> LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 8.dp,
                                    bottom = listBottomPadding
                                )
                            ) {
                                CookingStepsReadOnly(steps = recipe.steps)
                            }
                        }
                    }
                }
            }

            // Плавающие навигационные иконки — поверх всего, последними в Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clip(CornerShape)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            CornerShape
                        )
                        .gradientStyle(shape = CornerShape),
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        modifier = Modifier
                            .clip(CornerShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                CornerShape
                            )
                            .gradientStyle(shape = CornerShape),
                        onClick = {
                            val safeTitle = recipe.title
                                .replace(Regex("[^a-zA-Zа-яА-Я0-9 ]"), "")
                                .take(40)
                                .ifBlank { "recipe" }
                            val timestamp = SimpleDateFormat(
                                "yyyy-MM-dd_HHmm",
                                Locale.getDefault()
                            ).format(Date())
                            shareLauncher.launch("${safeTitle}_$timestamp.zip")
                        },
                        enabled = shareState !is RecipeShareState.InProgress
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Поделиться рецептом"
                        )
                    }
                    IconButton(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(CornerShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                CornerShape
                            )
                            .gradientStyle(shape = CornerShape),
                        onClick = { onEdit(recipeId) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Редактировать"
                        )
                    }
                }
            }
        }
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

    if (isAddToMenuSheetOpen) {
        AddToMenuBottomSheet(
            onDismiss = { viewModel.closeAddToMenuSheet() },
            onConfirm = { day, mealType -> viewModel.confirmAddToMenu(day, mealType) }
        )
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

@Composable
private fun FloatingCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .gradientStyle()
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun DescriptionPage(
    recipe: Recipe,
    bottomPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = bottomPadding
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        recipe.cookingMethod?.let { method ->
            ReadOnlyField(label = "Способ приготовления") {
                CookingMethodIcon(method = method, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(method.labelRes))
            }
        }
        recipe.cookingTimeMinutes?.let { minutes ->
            ReadOnlyField(label = "Время приготовления") {
                Icon(Icons.Filled.Schedule, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(formatCookingTime(minutes))
            }
        }
        ReadOnlyField(label = "Сложность") {
            Icon(Icons.Filled.Speed, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(recipe.difficulty.labelRes))
        }
        ReadOnlyField(label = "Описание") {
            Text(
                text = recipe.description.ifBlank { "Описание не указано" },
                color = if (recipe.description.isNotBlank()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun IngredientsPage(
    ingredients: List<RecipeIngredient>,
    fridgeItems: List<FridgeItem>,
    servings: Int,
    onServingsChange: (Int) -> Unit,
    onIngredientClick: (RecipeIngredient) -> Unit,
    onIngredientDelete: (RecipeIngredient) -> Unit,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = bottomPadding
        )
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Порции", style = MaterialTheme.typography.titleMedium)
                ServingsStepper(
                    servings = servings,
                    onDecrease = { onServingsChange((servings - 1).coerceAtLeast(1)) },
                    onIncrease = { onServingsChange((servings + 1).coerceAtMost(50)) }
                )
            }
        }
        items(ingredients, key = { it.id }) { ingredient ->
            IngredientViewRow(
                ingredient = ingredient,
                fridgeItems = fridgeItems,
                servings = servings,
                onClick = { if (!ingredient.product.isToTaste) onIngredientClick(ingredient) },
                onDelete = { onIngredientDelete(ingredient) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientViewRow(
    ingredient: RecipeIngredient,
    fridgeItems: List<FridgeItem>,
    servings: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        val scaledQuantity = ingredient.quantity * servings
        val status = ingredient.copy(quantity = scaledQuantity).availability(fridgeItems)
        val color = when (status) {
            IngredientAvailability.AVAILABLE -> MaterialTheme.colorScheme.primary
            IngredientAvailability.INSUFFICIENT -> MaterialTheme.colorScheme.error
        }

        ListItem(
            modifier = Modifier.clickableIf(!ingredient.product.isToTaste, onClick),
            leadingContent = {
                ProductIcon(
                    product = ingredient.product,
                    modifier = Modifier.size(40.dp)
                )
            },
            headlineContent = { Text(ingredient.product.localizedName()) },
            supportingContent = {
                Text(
                    if (ingredient.product.isToTaste) stringResource(R.string.to_taste)
                    else "${formatQty(scaledQuantity)} ${stringResource(ingredient.unit.labelRes)}",
                    color = if (ingredient.product.isToTaste) MaterialTheme.colorScheme.onSurfaceVariant else color
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                headlineColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

private fun Modifier.clickableIf(condition: Boolean, onClick: () -> Unit): Modifier =
    if (condition) this.then(Modifier.clickable(onClick = onClick)) else this

@Composable
private fun ServingsStepper(servings: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
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
            textAlign = TextAlign.Center
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
                FieldLabel("Количество (на 1 порцию)")
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
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToMenuBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (day: DayOfWeek, mealType: MealType) -> Unit
) {
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var selectedMeal by remember { mutableStateOf(MealType.BREAKFAST) }

    AppBottomSheet(title = "Добавить в меню", onDismissRequest = onDismiss) { onClose ->
        FieldLabel("День недели")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DayOfWeek.entries.toTypedArray()) { day ->
                FilterChip(
                    selected = day == selectedDay,
                    onClick = { selectedDay = day },
                    label = { Text(day.displayNameRu()) }
                )
            }
        }

        FieldLabel("Приём пищи")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MealType.entries.forEach { meal ->
                FilterChip(
                    selected = meal == selectedMeal,
                    onClick = { selectedMeal = meal },
                    label = { Text(meal.displayName) }
                )
            }
        }

        Button(
            onClick = { onConfirm(selectedDay, selectedMeal) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Добавить") }
    }
}

private fun DayOfWeek.displayNameRu(): String = when (this) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
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