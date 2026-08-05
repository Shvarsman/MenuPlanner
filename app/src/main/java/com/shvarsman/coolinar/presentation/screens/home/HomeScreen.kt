package com.shvarsman.coolinar.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCarouselSection
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.coolinar.presentation.ui.theme.molleFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenFridge: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenWeekMenu: () -> Unit,
    onShowAllSuggested: () -> Unit,
    onViewRecipe: (recipeId: Long) -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suggestedRecipes = uiState.suggestedRecipes
    val expiringFridgeItems = uiState.expiringFridgeItems
    val weeklyPlannedCount = uiState.weeklyPlannedCount
    val weeklyTotalCount = uiState.weeklyTotalCount
    val shoppingListCount = uiState.shoppingListCount
    val userName = uiState.userName

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Coolinar", fontSize = 24.sp, fontFamily = molleFont) },
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
                    text = greetingText(userName),
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

            item(key = "week_menu_nav") {
                WeekMenuNavCard(
                    planned = weeklyPlannedCount,
                    total = weeklyTotalCount,
                    onClick = onOpenWeekMenu,
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
        }
    }
}

private fun greetingText(userName: String?): String {
    val hour = java.time.LocalTime.now().hour
    val base = when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        else -> "Добрый вечер" // 18-23 и 0-4
    }
    return if (!userName.isNullOrBlank()) "$base, $userName!" else "$base!"
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
private fun WeekMenuNavCard(
    planned: Int,
    total: Int,
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
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "На следующей неделе запланировано $planned из $total приёмов пищи",
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