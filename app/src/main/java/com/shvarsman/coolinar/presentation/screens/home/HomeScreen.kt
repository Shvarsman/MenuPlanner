package com.shvarsman.coolinar.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.ReservedAmount
import com.shvarsman.coolinar.domain.model.ReservedKey
import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.presentation.screens.common.AppBottomSheet
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCarouselSection
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.coolinar.presentation.ui.theme.molleFont
import com.shvarsman.coolinar.presentation.utils.rememberDebouncedSearch
import com.shvarsman.coolinar.presentation.utils.rememberOptimisticDelete
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val weekDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)
private val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCreateRecipe: () -> Unit,
    onNavigateToCooking: (recipeId: Long, menuEntryId: Long) -> Unit,
    onOpenFridge: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onShowAllSuggested: () -> Unit,
    onViewRecipe: (recipeId: Long) -> Unit,
    onOpenBackup: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weekMenu = uiState.weekMenu
    val recipes = uiState.recipes
    val fridgeItems = uiState.fridgeItems
    val pickerTarget = uiState.pickerTarget
    val insufficientDialogEntry = uiState.insufficientDialogEntry
    val navigateToCooking = uiState.navigateToCooking
    val reservedQuantities = uiState.reservedQuantities
    val recipeSearchQuery = uiState.recipeSearchQuery
    val filteredPickerRecipes = uiState.filteredPickerRecipes
    val selectedDay = uiState.selectedDay
    val suggestedRecipes = uiState.suggestedRecipes
    val expiringFridgeItems = uiState.expiringFridgeItems
    val weeklyPlannedCount = uiState.weeklyPlannedCount
    val weeklyTotalCount = uiState.weeklyTotalCount
    val shoppingListCount = uiState.shoppingListCount

    val snackbarHostState = remember { SnackbarHostState() }
    val requestDelete = rememberOptimisticDelete<MenuEntry, Long>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { entry -> "«${entry.recipeTitle}» убран из меню" },
        onRequestDelete = { id -> viewModel.requestDeleteEntry(id) },
        onUndo = { id -> viewModel.undoDeleteEntry(id) }
    )

    LaunchedEffect(navigateToCooking) {
        navigateToCooking?.let { (recipeId, menuEntryId) ->
            onNavigateToCooking(recipeId, menuEntryId)
            viewModel.onNavigateToCookingConsumed()
        }
    }

    val entriesByKey = remember(weekMenu) { weekMenu.groupBy { it.dayOfWeek to it.mealType } }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Coolinar", fontSize = 24.sp, fontFamily = molleFont) },
                actions = {
                    IconButton(onClick = onOpenBackup) {
                        Icon(
                            Icons.Filled.SettingsBackupRestore,
                            contentDescription = "Резервное копирование"
                        )
                    }
                },
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + FloatingBottomBarClearance
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "greeting") {
                Text(
                    text = greetingText(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (expiringFridgeItems.isNotEmpty()) {
                item(key = "expiring_banner") {
                    ExpiringItemsBanner(
                        items = expiringFridgeItems,
                        onClick = onOpenFridge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item(key = "weekly_progress") {
                WeeklyProgressCard(
                    planned = weeklyPlannedCount,
                    total = weeklyTotalCount,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "shopping_list_quick_nav") {
                ShoppingListQuickNavCard(
                    itemCount = shoppingListCount,
                    onClick = onOpenShoppingList,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (suggestedRecipes.isNotEmpty()) {
                item(key = "suggested_carousel") {
                    RecipeCarouselSection(
                        title = "Можно приготовить прямо сейчас",
                        recipes = suggestedRecipes,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllSuggested
                    )
                }
            }

            item(key = "day_selector") {
                WeekDaySelector(
                    selectedDay = selectedDay,
                    onDaySelected = { viewModel.selectDay(it) },
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            mealTypes.forEach { meal ->
                item(key = "${selectedDay.name}_${meal.name}") {
                    MealSectionCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        meal = meal,
                        entries = entriesByKey[selectedDay to meal].orEmpty(),
                        recipes = recipes,
                        fridgeItems = fridgeItems,
                        reservedQuantities = reservedQuantities,
                        onAdd = { viewModel.openRecipePicker(selectedDay, meal) },
                        onRemove = { requestDelete(it) },
                        onCook = { viewModel.onCookClick(it) },
                        onView = { entry -> onViewRecipe(entry.recipeId) }
                    )
                }
            }
        }
    }

    if (pickerTarget != null) {
        RecipePickerDialog(
            filteredRecipes = filteredPickerRecipes,
            allRecipesEmpty = recipes.isEmpty(),
            fridgeItems = fridgeItems,
            reservedQuantities = reservedQuantities,
            searchQuery = recipeSearchQuery,
            onSearchQueryChange = { viewModel.onRecipeSearchQueryChange(it) },
            onDismiss = { viewModel.closeRecipePicker() },
            onSelect = { viewModel.assignRecipe(it) },
            onCreateNew = {
                viewModel.closeRecipePicker()
                onCreateRecipe()
            }
        )
    }

    if (insufficientDialogEntry != null) {
        InsufficientIngredientsDialog(
            onConfirmAnyway = { viewModel.confirmCookAnyway() },
            onGoToShopping = { viewModel.dismissInsufficientDialog() }
        )
    }
}

@Composable
private fun MealSectionCard(
    modifier: Modifier = Modifier,
    meal: MealType,
    entries: List<MenuEntry>,
    recipes: List<Recipe>,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<ReservedKey, ReservedAmount>,
    onAdd: () -> Unit,
    onRemove: (MenuEntry) -> Unit,
    onCook: (MenuEntry) -> Unit,
    onView: (MenuEntry) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meal.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Добавить блюдо на «${meal.displayName}»"
                )
            }
        }

        Surface(
            shape = CornerShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (entries.isEmpty()) {
                    Text(
                        "Не запланировано",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        entries.forEach { entry ->
                            val recipe = recipes.firstOrNull { it.id == entry.recipeId }
                            MenuEntryCard(
                                entry = entry,
                                recipe = recipe,
                                fridgeItems = fridgeItems,
                                reservedQuantities = reservedQuantities,
                                onRemove = { onRemove(entry) },
                                onCook = { onCook(entry) },
                                onView = { onView(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuEntryCard(
    modifier: Modifier = Modifier,
    entry: MenuEntry,
    recipe: Recipe?,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<ReservedKey, ReservedAmount>,
    onRemove: () -> Unit,
    onCook: () -> Unit,
    onView: () -> Unit
) {
    val allAvailable = remember(recipe, fridgeItems, reservedQuantities) {
        recipe != null && recipe.ingredients.isNotEmpty() && recipe.ingredients.all { ingredient ->
            val reserved = reservedQuantities[
                ReservedKey(ingredient.product.id, UnitConversion.canonicalUnit(ingredient.unit))
            ]
            ingredient.availability(fridgeItems, reserved) == IngredientAvailability.AVAILABLE
        }
    }

    Card(
        onClick = onView,
        modifier = modifier.fillMaxWidth(),
        shape = CornerShape
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.recipePhotoUri != null) {
                    AsyncImage(
                        model = rememberSizedImageRequest(entry.recipePhotoUri, 88.dp, 88.dp),
                        contentDescription = entry.recipeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(
                                RoundedCornerShape(
                                    topEnd = 28.dp,
                                    bottomEnd = 28.dp
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 28.dp,
                                    bottomStart = 28.dp
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.Restaurant,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }


                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        entry.recipeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    if (recipe != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                recipe.difficulty.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (recipe.ingredients.isNotEmpty()) {
                                Icon(
                                    if (allAvailable) Icons.Filled.Restaurant else Icons.Filled.Warning,
                                    contentDescription = if (allAvailable) "Все продукты есть" else "Не хватает продуктов",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (allAvailable) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onCook) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = "Приготовить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Убрать из меню")
                }
            }
        }
    }
}

@Composable
private fun InsufficientIngredientsDialog(
    onConfirmAnyway: () -> Unit,
    onGoToShopping: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onGoToShopping,
        title = { Text("Не хватает продуктов") },
        text = { Text("В холодильнике недостаточно ингредиентов для этого рецепта. Продолжить готовку или сначала докупить продукты?") },
        confirmButton = {
            TextButton(onClick = onConfirmAnyway) { Text("Всё равно продолжить") }
        },
        dismissButton = {
            TextButton(onClick = onGoToShopping) { Text("В магазин") }
        }
    )
}

@Composable
private fun RecipePickerDialog(
    filteredRecipes: List<Recipe>,
    allRecipesEmpty: Boolean,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<ReservedKey, ReservedAmount>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Recipe) -> Unit,
    onCreateNew: () -> Unit
) {
    var expandedRecipeId by remember { mutableStateOf<Long?>(null) }
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(
        searchQuery,
        onSearchQueryChange
    )

    AppBottomSheet(
        title = "Выбрать рецепт",
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { _ ->
        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = onLocalSearchQueryChange,
            placeholder = { Text("Поиск рецептов") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (localSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onLocalSearchQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            shape = CornerShape,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Новый рецепт")
        }

        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (allRecipesEmpty) "У вас пока нет рецептов" else "Ничего не найдено",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipePickerCard(
                        recipe = recipe,
                        isExpanded = expandedRecipeId == recipe.id,
                        onToggleExpand = {
                            expandedRecipeId =
                                if (expandedRecipeId == recipe.id) null else recipe.id
                        },
                        onSelect = { onSelect(recipe) },
                        fridgeItems = fridgeItems,
                        reservedQuantities = reservedQuantities
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipePickerCard(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<ReservedKey, ReservedAmount>
) {

    val hasIngredients = recipe.ingredients.isNotEmpty()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CornerShape
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .then(
                        if (hasIngredients) {
                            Modifier.clickable { onToggleExpand() }
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.photoUri != null) {
                    AsyncImage(
                        model = rememberSizedImageRequest(recipe.photoUri, 88.dp, 88.dp),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(
                                RoundedCornerShape(
                                    topEnd = 28.dp,
                                    bottomEnd = 28.dp
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 28.dp,
                                    bottomStart = 28.dp
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.6f
                                    )
                                )
                            }
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                Button(
                    modifier = Modifier.padding(end = 12.dp),
                    onClick = onSelect
                ) {
                    Text("Выбрать")
                }
            }

            if (isExpanded && hasIngredients) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    )
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    recipe.ingredients.forEach { ingredient ->
                        val reserved = reservedQuantities[
                            ReservedKey(
                                productId = ingredient.product.id,
                                canonicalUnit = UnitConversion.canonicalUnit(ingredient.unit)
                            )
                        ]
                        val status = ingredient.availability(fridgeItems, reserved)
                        val color = when (status) {
                            IngredientAvailability.AVAILABLE -> MaterialTheme.colorScheme.primary
                            IngredientAvailability.INSUFFICIENT -> MaterialTheme.colorScheme.error
                        }
                        Text(
                            text = "${ingredient.product.name} — ${formatQty(ingredient.quantity)} ${ingredient.unit.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDaySelector(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekStart =
        remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val today = remember { LocalDate.now().dayOfWeek }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(weekDays) { index, day ->
            val date = weekStart.plusDays(index.toLong())
            val isSelected = day == selectedDay
            val isToday = day == today

            Surface(
                onClick = { onDaySelected(day) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                modifier = Modifier.width(56.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (isToday) {
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun greetingText(): String {
    val hour = java.time.LocalTime.now().hour
    val base = when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        else -> "Добрый вечер" // 18-23 и 0-4
    }
    // Имени пока нет — функционал входа появится позже; тогда сюда добавится ", $userName"
    return "$base!"
}

@Composable
private fun ExpiringItemsBanner(
    items: List<FridgeItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Скоро истекает срок годности",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    items.joinToString(
                        limit = 3,
                        truncated = "и ещё ${items.size - 3}..."
                    ) { it.product.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    planned: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = CornerShape) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Запланировано $planned из $total приёмов пищи",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else planned.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun ShoppingListQuickNavCard(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = CornerShape) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (itemCount == 0) "Список покупок пуст" else "В списке покупок: $itemCount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()