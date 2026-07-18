package com.shvarsman.menuplanner.presentation.screens.menu

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.ReservedAmount
import com.shvarsman.menuplanner.domain.model.ReservedKey
import com.shvarsman.menuplanner.domain.model.UnitConversion
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.screens.common.AppBottomSheet
import com.shvarsman.menuplanner.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import com.shvarsman.menuplanner.presentation.ui.theme.molleFont
import com.shvarsman.menuplanner.presentation.utils.rememberDebouncedSearch
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val weekDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)
private val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onCreateRecipe: () -> Unit,
    onNavigateToCooking: (recipeId: Long, menuEntryId: Long) -> Unit,
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

    // Стейты управления боковой панелью
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(navigateToCooking) {
        navigateToCooking?.let { (recipeId, menuEntryId) ->
            onNavigateToCooking(recipeId, menuEntryId)
            viewModel.onNavigateToCookingConsumed()
        }
    }

    val entriesByKey = remember(weekMenu) {
        weekMenu.groupBy { it.dayOfWeek to it.mealType }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Выбор дня недели",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                weekDays.forEach { day ->
                    val isToday = day == LocalDate.now().dayOfWeek
                    val dayName = day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"))
                        .replaceFirstChar { it.uppercase() }
                    val label = if (isToday) "$dayName (Сегодня)" else dayName

                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = day == selectedDay,
                        onClick = {
                            viewModel.selectDay(day)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Coolinar",
                            fontSize = 24.sp,
                            fontFamily = molleFont
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Открыть выбор дней"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenBackup) {
                            Icon(
                                imageVector = Icons.Filled.SettingsBackupRestore,
                                contentDescription = "Резервное копирование"
                            )
                        }
                    },
                    expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                    start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Отображаем только выбранный день
                item(key = selectedDay.name) {
                    DayCard(
                        day = selectedDay,
                        entriesByMeal = mealTypes.associateWith { meal ->
                            entriesByKey[selectedDay to meal].orEmpty()
                        },
                        onAddMeal = { meal -> viewModel.openRecipePicker(selectedDay, meal) },
                        onRemoveEntry = { viewModel.removeEntry(it) },
                        onCookEntry = { viewModel.onCookClick(it) },
                        onViewEntry = { entry -> onViewRecipe(entry.recipeId) }
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
private fun DayCard(
    modifier: Modifier = Modifier,
    day: DayOfWeek,
    entriesByMeal: Map<MealType, List<MenuEntry>>,
    onAddMeal: (MealType) -> Unit,
    onRemoveEntry: (MenuEntry) -> Unit,
    onCookEntry: (MenuEntry) -> Unit,
    onViewEntry: (MenuEntry) -> Unit
) {
    val isToday = day == LocalDate.now().dayOfWeek
    val dayName = day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"))
        .replaceFirstChar { it.uppercase() }
    val titleText = if (isToday) "$dayName (Сегодня)" else dayName

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            mealTypes.forEach { meal ->
                MealRow(
                    meal = meal,
                    entries = entriesByMeal[meal].orEmpty(),
                    onAdd = { onAddMeal(meal) },
                    onRemove = onRemoveEntry,
                    onCook = onCookEntry,
                    onView = onViewEntry
                )
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: MealType,
    entries: List<MenuEntry>,
    onAdd: () -> Unit,
    onRemove: (MenuEntry) -> Unit,
    onCook: (MenuEntry) -> Unit,
    onView: (MenuEntry) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                meal.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить блюдо")
            }
        }
        if (entries.isEmpty()) {
            Text(
                "Не запланировано",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEach { entry ->
                    MenuEntryCard(
                        entry = entry,
                        onRemove = { onRemove(entry) },
                        onCook = { onCook(entry) },
                        onView = { onView(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuEntryCard(
    entry: MenuEntry,
    onRemove: () -> Unit,
    onCook: () -> Unit,
    onView: () -> Unit
) {
    Card(
        onClick = onView,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
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
                                topEnd = AppCornerRadius,
                                bottomEnd = AppCornerRadius
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
                                topStart = AppCornerRadius,
                                bottomStart = AppCornerRadius
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

            Text(
                entry.recipeTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

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
            shape = RoundedCornerShape(AppCornerRadius),
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
        shape = RoundedCornerShape(AppCornerRadius)
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
                                    topEnd = AppCornerRadius,
                                    bottomEnd = AppCornerRadius
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
                                    topStart = AppCornerRadius,
                                    bottomStart = AppCornerRadius
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
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
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
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
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

private fun formatQty(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()