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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.IngredientAvailability
import com.shvarsman.coolinar.domain.model.MealType
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.model.Recipe
import com.shvarsman.coolinar.domain.model.ReservedAmount
import com.shvarsman.coolinar.domain.model.ReservedKey
import com.shvarsman.coolinar.domain.model.UnitConversion
import com.shvarsman.coolinar.domain.model.availability
import com.shvarsman.coolinar.domain.model.computeReservedAmounts
import com.shvarsman.coolinar.presentation.screens.common.AppBottomSheet
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.NavRow
import com.shvarsman.coolinar.presentation.screens.common.rememberSizedImageRequest
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.utils.rememberDebouncedSearch
import com.shvarsman.coolinar.presentation.utils.rememberOptimisticDelete
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters

private val weekDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)
private val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekMenuScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCreateRecipe: () -> Unit,
    onOpenCookSelection: () -> Unit,
    onViewRecipe: (recipeId: String) -> Unit,
    onOpenShoppingList: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weekMenu = uiState.weekMenu
    val recipes = uiState.recipes
    val fridgeItems = uiState.fridgeItems
    val pickerTarget = uiState.pickerTarget
    val reservedQuantities = uiState.reservedQuantities
    val recipeSearchQuery = uiState.recipeSearchQuery
    val filteredPickerRecipes = uiState.filteredPickerRecipes
    val selectedDay = uiState.selectedDay
    val selectedWeekOffset = uiState.selectedWeekOffset

    val entryRemovedTemplate = stringResource(R.string.menu_entry_removed)

    val snackbarHostState = remember { SnackbarHostState() }
    val requestDelete = rememberOptimisticDelete<MenuEntry, String>(
        snackbarHostState = snackbarHostState,
        idOf = { it.id },
        message = { entry -> String.format(entryRemovedTemplate, entry.recipeTitle) },
        onRequestDelete = { id -> viewModel.requestDeleteEntry(id) },
        onUndo = { id -> viewModel.undoDeleteEntry(id) }
    )

    val entriesByKey = remember(weekMenu) { weekMenu.groupBy { it.dayOfWeek to it.mealType } }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                NavRow(
                    icon = Icons.Filled.Restaurant,
                    text = stringResource(R.string.start_cooking),
                    modifier = Modifier.padding(16.dp),
                    onClick = onOpenCookSelection
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.week_menu_title)) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
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
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "week_switcher") {
                WeekSwitcher(
                    selectedWeekOffset = selectedWeekOffset,
                    onWeekSelected = { viewModel.selectWeek(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item(key = "day_selector") {
                WeekDaySelector(
                    weekStart = LocalDate.now()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .plusWeeks(selectedWeekOffset.toLong()),
                    selectedDay = selectedDay,
                    onDaySelected = { viewModel.selectDay(it) }
                )
            }
            mealTypes.forEach { meal ->
                item(key = "${selectedDay.name}_${meal.name}") {
                    MealSectionCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        meal = meal,
                        entries = entriesByKey[selectedDay to meal].orEmpty(),
                        weekMenu = weekMenu,
                        recipes = recipes,
                        fridgeItems = fridgeItems,
                        onAdd = { viewModel.openRecipePicker(selectedDay, meal) },
                        onRemove = { requestDelete(it) },
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
}

@Composable
private fun WeekDaySelector(
    weekStart: LocalDate,
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(weekDays) { index, day ->
            val date = weekStart.plusDays(index.toLong())
            val isSelected = day == selectedDay
            val isToday = date == today

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
                        text = day.getDisplayName(
                            TextStyle.SHORT,
                            LocalLocale.current.platformLocale
                        )
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

@Composable
private fun WeekSwitcher(
    selectedWeekOffset: Int,
    onWeekSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedWeekOffset == 0,
            onClick = { onWeekSelected(0) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text(stringResource(R.string.this_week))
        }
        SegmentedButton(
            selected = selectedWeekOffset == 1,
            onClick = { onWeekSelected(1) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text(stringResource(R.string.next_week))
        }
    }
}

@Composable
private fun MealSectionCard(
    modifier: Modifier = Modifier,
    meal: MealType,
    entries: List<MenuEntry>,
    weekMenu: List<MenuEntry>,
    recipes: List<Recipe>,
    fridgeItems: List<FridgeItem>,
    onAdd: () -> Unit,
    onRemove: (MenuEntry) -> Unit,
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
                    contentDescription = stringResource(R.string.add_meal_for, meal.displayName)
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
                        text = stringResource(R.string.not_planned),
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
                                weekMenu = weekMenu,
                                recipes = recipes,
                                fridgeItems = fridgeItems,
                                onRemove = { onRemove(entry) },
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
    weekMenu: List<MenuEntry>,
    recipes: List<Recipe>,
    fridgeItems: List<FridgeItem>,
    onRemove: () -> Unit,
    onView: () -> Unit
) {
    val allAvailable = remember(recipe, fridgeItems, weekMenu, recipes) {
        recipe != null && recipe.ingredients.isNotEmpty() && run {
            val reservedFromEarlierEntries = computeReservedAmounts(
                weekMenu.filter { it.createdAt < entry.createdAt },
                recipes
            )
            recipe.ingredients.all { ingredient ->
                val reserved = reservedFromEarlierEntries[
                    ReservedKey(
                        ingredient.product.id,
                        UnitConversion.canonicalUnit(ingredient.unit)
                    )
                ]
                ingredient.availability(fridgeItems, reserved) == IngredientAvailability.AVAILABLE
            }
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
                                text = stringResource(recipe.difficulty.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (recipe.ingredients.isNotEmpty()) {
                                Icon(
                                    if (allAvailable) Icons.Filled.Restaurant else Icons.Filled.Warning,
                                    contentDescription = if (allAvailable) {
                                        stringResource(R.string.all_products_available)
                                    } else {
                                        stringResource(R.string.products_insufficient)
                                    },
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

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.remove_from_menu)
                    )
                }
            }
        }
    }
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
    var expandedRecipeId by remember { mutableStateOf<String?>(null) }
    val (localSearchQuery, onLocalSearchQueryChange) = rememberDebouncedSearch(
        searchQuery,
        onSearchQueryChange
    )

    AppBottomSheet(
        title = stringResource(R.string.select_recipe),
        fillMaxHeight = true,
        onDismissRequest = onDismiss
    ) { _ ->
        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = onLocalSearchQueryChange,
            placeholder = { Text(stringResource(R.string.search_recipes)) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (localSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onLocalSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
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
            Text(stringResource(R.string.new_recipe))
        }

        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (allRecipesEmpty) {
                        stringResource(R.string.no_recipes_yet)
                    } else {
                        stringResource(R.string.nothing_found)
                    },
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
                    Text(stringResource(R.string.select))
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
                            text = stringResource(
                                R.string.ingredient_with_qty,
                                ingredient.product.name,
                                formatQty(ingredient.quantity),
                                stringResource(ingredient.unit.labelRes)
                            ),
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