package com.shvarsman.menuplanner.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shvarsman.menuplanner.presentation.catalog.ProductCatalogScreen
import com.shvarsman.menuplanner.presentation.cooking.CookingScreen
import com.shvarsman.menuplanner.presentation.fridge.FridgeScreen
import com.shvarsman.menuplanner.presentation.menu.MenuScreen
import com.shvarsman.menuplanner.presentation.recipe.RecipeEditorScreen
import com.shvarsman.menuplanner.presentation.recipe.RecipeListScreen
import com.shvarsman.menuplanner.presentation.recipe.RecipeViewScreen
import com.shvarsman.menuplanner.presentation.shoppinglist.ShoppingListScreen

private data class BottomItem(
    val destination: Destination,
    val label: String,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Destination.Menu, "Меню", Icons.Filled.RestaurantMenu),
    BottomItem(Destination.Fridge, "Холодильник", Icons.Filled.Kitchen),
    BottomItem(Destination.Recipes, "Рецепты", Icons.AutoMirrored.Filled.MenuBook),
    BottomItem(Destination.ShoppingList, "Покупки", Icons.Filled.ShoppingCart)
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomItems.forEach { item ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == item.destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Menu.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Destination.Menu.route) {
                MenuScreen(
                    onNavigateToRecipes = { navController.navigate(Destination.Recipes.route) },
                    onNavigateToCooking = { recipeId, menuEntryId ->
                        navController.navigate(Destination.Cooking.createRoute(recipeId, menuEntryId))
                    }
                )
            }
            composable(Destination.Fridge.route) {
                FridgeScreen(onOpenCatalog = { navController.navigate(Destination.ProductCatalog.route) })
            }
            composable(Destination.ProductCatalog.route) {
                ProductCatalogScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Recipes.route) {
                RecipeListScreen(
                    onAddRecipe = {
                        navController.navigate(Destination.RecipeEditor.createRoute(Destination.RecipeEditor.NEW_RECIPE_ID))
                    },
                    onViewRecipe = { id ->
                        navController.navigate(Destination.RecipeView.createRoute(id))
                    },
                    onEditRecipe = { id ->
                        navController.navigate(Destination.RecipeEditor.createRoute(id))
                    }
                )
            }
            composable(
                route = Destination.RecipeView.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                RecipeViewScreen(
                    recipeId = recipeId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate(Destination.RecipeEditor.createRoute(id))
                    }
                )
            }
            composable(
                route = Destination.RecipeEditor.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                RecipeEditorScreen(
                    recipeId = recipeId,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Destination.ShoppingList.route) {
                ShoppingListScreen()
            }
            composable(
                route = Destination.Cooking.route,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.LongType },
                    navArgument("menuEntryId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                val menuEntryId = backStackEntry.arguments?.getLong("menuEntryId") ?: 0L
                CookingScreen(
                    recipeId = recipeId,
                    menuEntryId = menuEntryId,
                    onBack = { navController.popBackStack() },
                    onFinished = { navController.popBackStack() }
                )
            }
        }
    }
}
