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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.presentation.screens.common.NavRow
import com.shvarsman.coolinar.presentation.screens.recipe.components.RecipeCarouselSection
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
    onViewRecipe: (recipeId: String) -> Unit,
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
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        fontFamily = molleFont
                    )
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
                NavRow(
                    icon = Icons.Filled.CalendarMonth,
                    text = stringResource(
                        R.string.week_menu_summary,
                        weeklyPlannedCount,
                        weeklyTotalCount
                    ),
                    onClick = onOpenWeekMenu,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "shopping_list_quick_nav") {
                NavRow(
                    icon = Icons.Filled.ShoppingCart,
                    text = if (shoppingListCount == 0) {
                        stringResource(R.string.shopping_list_empty_short)
                    } else {
                        stringResource(R.string.shopping_list_count, shoppingListCount)
                    },
                    onClick = onOpenShoppingList,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (suggestedRecipes.isNotEmpty()) {
                item(key = "suggested_carousel") {
                    RecipeCarouselSection(
                        title = stringResource(R.string.can_cook_now),
                        recipes = suggestedRecipes,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllSuggested
                    )
                }
            }
        }
    }
}

@Composable
private fun greetingText(userName: String?): String {
    val hour = java.time.LocalTime.now().hour
    val base = when (hour) {
        in 5..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
    return if (!userName.isNullOrBlank()) {
        stringResource(R.string.greeting_with_name, base, userName)
    } else stringResource(R.string.greeting_no_name, base)
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
                    text = stringResource(R.string.expiring_soon_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    items.joinToString(
                        limit = 3,
                        truncated = stringResource(R.string.and_more, items.size - 3)
                    ) { it.product.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2
                )
            }
        }
    }
}