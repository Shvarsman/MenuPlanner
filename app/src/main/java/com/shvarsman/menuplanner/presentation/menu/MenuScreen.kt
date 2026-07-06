package com.shvarsman.menuplanner.presentation.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.model.Recipe
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
    viewModel: MenuViewModel = hiltViewModel()
) {
    val weekMenu by viewModel.weekMenu.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val pickerTarget by viewModel.pickerTarget.collectAsState()

    val entriesByKey = remember(weekMenu) {
        weekMenu.groupBy { it.dayOfWeek to it.mealType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Меню на неделю",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weekDays) { day ->
                DayCard(
                    day = day,
                    entriesByMeal = mealTypes.associateWith { meal -> entriesByKey[day to meal].orEmpty() },
                    onAddMeal = { meal -> viewModel.openRecipePicker(day, meal) },
                    onRemoveEntry = { viewModel.removeEntry(it) }
                )
            }
        }
    }

    if (pickerTarget != null) {
        RecipePickerDialog(
            recipes = recipes,
            onDismiss = { viewModel.closeRecipePicker() },
            onSelect = { viewModel.assignRecipe(it) },
            onCreateNew = {
                viewModel.closeRecipePicker()
                onNavigateToRecipes()
            }
        )
    }
}

@Composable
private fun DayCard(
    day: DayOfWeek,
    entriesByMeal: Map<MealType, List<MenuEntry>>,
    onAddMeal: (MealType) -> Unit,
    onRemoveEntry: (MenuEntry) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                    onRemove = onRemoveEntry
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
    onRemove: (MenuEntry) -> Unit
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
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(entry.recipeTitle, style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onRemove(entry) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Убрать из меню")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipePickerDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onSelect: (Recipe) -> Unit,
    onCreateNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать рецепт") },
        text = {
            if (recipes.isEmpty()) {
                Text("У вас пока нет рецептов. Создайте первый рецепт во вкладке «Рецепты».")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
                        ListItem(
                            headlineContent = { Text(recipe.title) },
                            modifier = Modifier.clickable { onSelect(recipe) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNew) { Text("Новый рецепт") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
