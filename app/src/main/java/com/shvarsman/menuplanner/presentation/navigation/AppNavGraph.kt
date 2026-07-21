package com.shvarsman.menuplanner.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shvarsman.menuplanner.presentation.screens.backup.BackupScreen
import com.shvarsman.menuplanner.presentation.screens.catalog.ProductCatalogScreen
import com.shvarsman.menuplanner.presentation.screens.cooking.CookingScreen
import com.shvarsman.menuplanner.presentation.screens.fridge.FridgeScreen
import com.shvarsman.menuplanner.presentation.screens.menu.MenuScreen
import com.shvarsman.menuplanner.presentation.screens.recipe.AllCategoriesScreen
import com.shvarsman.menuplanner.presentation.screens.recipe.RecipeCategoryScreen
import com.shvarsman.menuplanner.presentation.screens.recipe.RecipeListScreen
import com.shvarsman.menuplanner.presentation.screens.recipe.RecipeViewScreen
import com.shvarsman.menuplanner.presentation.screens.recipeeditor.RecipeEditorScreen
import com.shvarsman.menuplanner.presentation.screens.shoppinglist.ShoppingListScreen

private data class BottomItem(
    val destination: Destination,
    val label: String,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Destination.Menu, "Главная", Icons.Filled.RestaurantMenu),
    BottomItem(Destination.Fridge, "Холодильник", Icons.Filled.Kitchen),
    BottomItem(Destination.Recipes, "Рецепты", Icons.AutoMirrored.Filled.MenuBook),
    BottomItem(Destination.ShoppingList, "Покупки", Icons.Filled.ShoppingCart)
)

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = rootNavController,
            startDestination = "main_tabs_wrapper",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        ) {
            composable("main_tabs_wrapper") {
                MainTabsScreen(rootNavController = rootNavController)
            }

            composable(Destination.ProductCatalog.route) {
                ProductCatalogScreen(onBack = { rootNavController.popBackStack() })
            }

            composable(
                route = Destination.RecipeCategoryList.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) {
                RecipeCategoryScreen(
                    onBack = { rootNavController.popBackStack() },
                    onViewRecipe = { id ->
                        rootNavController.navigate(Destination.RecipeView.createRoute(id))
                    },
                    onEditRecipe = { id ->
                        rootNavController.navigate(Destination.RecipeEditor.createRoute(id))
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
                    onBack = { rootNavController.popBackStack() },
                    onEdit = { id ->
                        rootNavController.navigate(Destination.RecipeEditor.createRoute(id))
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
                    onDone = { rootNavController.popBackStack() }
                )
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
                    onBack = { rootNavController.popBackStack() },
                    onFinished = { rootNavController.popBackStack() }
                )
            }

            composable(Destination.Backup.route) {
                BackupScreen(onBack = { rootNavController.popBackStack() })
            }

            composable(Destination.AllCategories.route) {
                AllCategoriesScreen(
                    onBack = { rootNavController.popBackStack() },
                    onCategoryClick = { category ->
                        rootNavController.navigate(
                            Destination.RecipeCategoryList.createRoute(
                                category
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MainTabsScreen(rootNavController: NavHostController) {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.destination.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            childNavController.navigate(item.destination.route) {
                                popUpTo(childNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = childNavController,
            startDestination = Destination.Menu.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Destination.Menu.route) {
                MenuScreen(
                    onCreateRecipe = {
                        rootNavController.navigate(
                            Destination.RecipeEditor.createRoute(Destination.RecipeEditor.NEW_RECIPE_ID)
                        )
                    },
                    onNavigateToCooking = { recipeId, menuEntryId ->
                        rootNavController.navigate(
                            Destination.Cooking.createRoute(recipeId, menuEntryId)
                        )
                    },
                    onViewRecipe = { recipeId ->
                        rootNavController.navigate(Destination.RecipeView.createRoute(recipeId))
                    },
                    onOpenBackup = { rootNavController.navigate(Destination.Backup.route) }
                )
            }

            composable(Destination.Fridge.route) {
                FridgeScreen(onOpenCatalog = { rootNavController.navigate(Destination.ProductCatalog.route) })
            }

            composable(Destination.Recipes.route) {
                RecipeListScreen(
                    onAddRecipe = {
                        rootNavController.navigate(
                            Destination.RecipeEditor.createRoute(Destination.RecipeEditor.NEW_RECIPE_ID)
                        )
                    },
                    onViewRecipe = { id ->
                        rootNavController.navigate(
                            Destination.RecipeView.createRoute(
                                id
                            )
                        )
                    },
                    onEditRecipe = { id ->
                        rootNavController.navigate(
                            Destination.RecipeEditor.createRoute(
                                id
                            )
                        )
                    },
                    onCategoryClick = { category ->
                        rootNavController.navigate(
                            Destination.RecipeCategoryList.createRoute(
                                category
                            )
                        )
                    },
                    onShowAllCategories = { rootNavController.navigate(Destination.AllCategories.route) } // добавить
                )
            }

            composable(Destination.ShoppingList.route) {
                ShoppingListScreen()
            }
        }
    }
}