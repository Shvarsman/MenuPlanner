package com.shvarsman.menuplanner.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.IngredientAvailability
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
import com.shvarsman.menuplanner.domain.model.ReservedAmount
import com.shvarsman.menuplanner.domain.model.availability
import com.shvarsman.menuplanner.presentation.ui.theme.AppCornerRadius
import java.time.DayOfWeek
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
    onNavigateToRecipes: () -> Unit,
    onNavigateToCooking: (recipeId: Long, menuEntryId: Long) -> Unit,
    onViewRecipe: (recipeId: Long) -> Unit,
    onOpenBackup: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val weekMenu by viewModel.weekMenu.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val fridgeItems by viewModel.fridgeItems.collectAsState()
    val pickerTarget by viewModel.pickerTarget.collectAsState()
    val insufficientDialogEntry by viewModel.insufficientDialogEntry.collectAsState()
    val navigateToCooking by viewModel.navigateToCooking.collectAsState()
    val reservedQuantities by viewModel.reservedQuantities.collectAsState()
    val recipeSearchQuery by viewModel.recipeSearchQuery.collectAsState()

    LaunchedEffect(navigateToCooking) {
        navigateToCooking?.let { (recipeId, menuEntryId) ->
            onNavigateToCooking(recipeId, menuEntryId)
            viewModel.onNavigateToCookingConsumed()
        }
    }

    val entriesByKey = remember(weekMenu) {
        weekMenu.groupBy { it.dayOfWeek to it.mealType }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Меню на неделю",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(onClick = onOpenBackup) {
                        Icon(Icons.Filled.SettingsBackupRestore, contentDescription = "Резервное копирование")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weekDays) { day ->
                DayCard(
                    day = day,
                    entriesByMeal = mealTypes.associateWith { meal -> entriesByKey[day to meal].orEmpty() },
                    onAddMeal = { meal -> viewModel.openRecipePicker(day, meal) },
                    onRemoveEntry = { viewModel.removeEntry(it) },
                    onCookEntry = { viewModel.onCookClick(it) },
                    onViewEntry = { entry -> onViewRecipe(entry.recipeId) }
                )
            }
        }
    }

    if (pickerTarget != null) {
        RecipePickerDialog(
            recipes = recipes,
            fridgeItems = fridgeItems,
            reservedQuantities = reservedQuantities,
            searchQuery = recipeSearchQuery,
            onSearchQueryChange = { viewModel.onRecipeSearchQueryChange(it) },
            onDismiss = { viewModel.closeRecipePicker() },
            onSelect = { viewModel.assignRecipe(it) },
            onCreateNew = {
                viewModel.closeRecipePicker()
                onNavigateToRecipes()
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
    day: DayOfWeek,
    entriesByMeal: Map<MealType, List<MenuEntry>>,
    onAddMeal: (MealType) -> Unit,
    onRemoveEntry: (MenuEntry) -> Unit,
    onCookEntry: (MenuEntry) -> Unit,
    onViewEntry: (MenuEntry) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"))
                    .replaceFirstChar { it.uppercase() },
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
    ElevatedCard(
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
                    model = entry.recipePhotoUri,
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
    recipes: List<Recipe>,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<Long, ReservedAmount>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Recipe) -> Unit,
    onCreateNew: () -> Unit
) {
    var expandedRecipeId by remember { mutableStateOf<Long?>(null) }

    val filteredRecipes = remember(recipes, searchQuery) {
        if (searchQuery.isBlank()) recipes
        else recipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(AppCornerRadius),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Выбрать рецепт", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть")
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск рецептов") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(AppCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onCreateNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
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
                            if (recipes.isEmpty()) "У вас пока нет рецептов" else "Ничего не найдено",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
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
    }
}

@Composable
private fun RecipePickerCard(
    recipe: Recipe,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
    fridgeItems: List<FridgeItem>,
    reservedQuantities: Map<Long, ReservedAmount>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCornerRadius)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.photoUri != null) {
                    AsyncImage(
                        model = recipe.photoUri,
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                Button(
                    onClick = onSelect,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text("Выбрать")
                }
            }

            // Превью ингредиентов с цветовой индикацией наличия в холодильнике
            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    recipe.ingredients.forEach { ingredient ->
                        val reserved = reservedQuantities[ingredient.product.id]
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