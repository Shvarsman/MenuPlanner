package com.shvarsman.coolinar.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotPose
import com.shvarsman.coolinar.presentation.screens.common.MascotWelcomeTip
import com.shvarsman.coolinar.presentation.screens.common.NavRow
import com.shvarsman.coolinar.presentation.screens.common.StatCard
import com.shvarsman.coolinar.presentation.screens.common.localizedName
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
    onShowAllRecipes: () -> Unit,
    onViewRecipe: (recipeId: String) -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suggestedRecipes = uiState.suggestedRecipes
    val expiringFridgeItems = uiState.expiringFridgeItems
    val weeklyPlannedCount = uiState.weeklyPlannedCount
    val weeklyTotalCount = uiState.weeklyTotalCount
    val shoppingListCount = uiState.shoppingListCount
    val totalRecipesCount = uiState.recipes.size
    val userName = uiState.userName

    MascotWelcomeTip(
        tipId = "home_intro",
        message = stringResource(R.string.mascot_tip_home),
        enabled = uiState.recipes.isNotEmpty() || expiringFridgeItems.isNotEmpty()
    )

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MascotImage(
                        pose = greetingMascotPose(),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = greetingText(userName),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = homeStatusSubtitle(
                                expiringCount = expiringFridgeItems.size,
                                weeklyPlannedCount = weeklyPlannedCount,
                                weeklyTotalCount = weeklyTotalCount
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item(key = "stat_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = ImageVector.vectorResource(R.drawable.menu),
                        value = stringResource(
                            R.string.week_menu_summary,
                            weeklyPlannedCount,
                            weeklyTotalCount
                        ),
                        label = stringResource(R.string.week_menu_title),
                        progress = if (weeklyTotalCount > 0) {
                            weeklyPlannedCount / weeklyTotalCount.toFloat()
                        } else null,
                        onClick = onOpenWeekMenu,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = ImageVector.vectorResource(R.drawable.shopping_list),
                            value = if (shoppingListCount == 0) {
                                "—"
                            } else {
                                shoppingListCount.toString()
                            },
                            label = stringResource(R.string.shopping_list_title),
                            onClick = onOpenShoppingList,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        StatCard(
                            icon = ImageVector.vectorResource(R.drawable.recipes),
                            value = totalRecipesCount.toString(),
                            label = stringResource(R.string.total_recipes_title),
                            onClick = onShowAllRecipes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            }

            item(key = "expiring_banner") {
                if (expiringFridgeItems.isNotEmpty()) {
                    ExpiringItemsBanner(
                        items = expiringFridgeItems,
                        onClick = onOpenFridge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    FreshFridgeBanner(
                        onClick = onOpenFridge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item(key = "suggested_carousel") {
                if (suggestedRecipes.isNotEmpty()) {
                    RecipeCarouselSection(
                        title = stringResource(R.string.can_cook_now),
                        recipes = suggestedRecipes,
                        onRecipeClick = onViewRecipe,
                        onShowAllClick = onShowAllSuggested
                    )
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(R.string.can_cook_now),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        NavRow(
                            icon = ImageVector.vectorResource(R.drawable.fridge),
                            text = stringResource(R.string.suggested_empty_cta),
                            onClick = onOpenFridge
                        )
                    }
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
private fun greetingMascotPose(): MascotPose {
    val hour = java.time.LocalTime.now().hour
    return if (hour in 6..21) MascotPose.WAVING else MascotPose.SLEEPY
}

@Composable
private fun homeStatusSubtitle(
    expiringCount: Int,
    weeklyPlannedCount: Int,
    weeklyTotalCount: Int
): String = when {
    expiringCount > 0 -> pluralStringResource(
        R.plurals.status_expiring_soon,
        expiringCount,
        expiringCount
    )

    weeklyPlannedCount < weeklyTotalCount -> stringResource(R.string.status_menu_incomplete)
    else -> stringResource(R.string.status_all_good)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
        shape = CornerShape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.expiring_soon_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onError
                )
                Text(
                    items.joinToString(
                        limit = 3,
                        truncated = stringResource(R.string.and_more, items.size - 3)
                    ) { it.product.localizedName() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onError,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(12.dp))
            MascotImage(
                pose = MascotPose.WORRIED,
                modifier = Modifier.size(40.dp)
            )

        }
    }
}

@Composable
private fun FreshFridgeBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = CornerShape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.fridge_all_fresh),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            MascotImage(
                pose = MascotPose.HAPPY,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}