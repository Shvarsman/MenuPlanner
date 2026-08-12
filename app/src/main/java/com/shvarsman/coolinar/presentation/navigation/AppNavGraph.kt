package com.shvarsman.coolinar.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.backup.BackupScreen
import com.shvarsman.coolinar.presentation.screens.catalog.ProductCatalogScreen
import com.shvarsman.coolinar.presentation.screens.cooking.CookingScreen
import com.shvarsman.coolinar.presentation.screens.cookselection.CookSelectionScreen
import com.shvarsman.coolinar.presentation.screens.fridge.FridgeScreen
import com.shvarsman.coolinar.presentation.screens.home.HomeScreen
import com.shvarsman.coolinar.presentation.screens.home.WeekMenuScreen
import com.shvarsman.coolinar.presentation.screens.onboarding.OnboardingScreen
import com.shvarsman.coolinar.presentation.screens.profile.AuthScreen
import com.shvarsman.coolinar.presentation.screens.profile.ProfileScreen
import com.shvarsman.coolinar.presentation.screens.profile.ProfileSettingsScreen
import com.shvarsman.coolinar.presentation.screens.recipe.all.AllRecipesListScreen
import com.shvarsman.coolinar.presentation.screens.recipe.category.AllCategoriesScreen
import com.shvarsman.coolinar.presentation.screens.recipe.category.RecipeCategoryScreen
import com.shvarsman.coolinar.presentation.screens.recipe.editor.RecipeEditorScreen
import com.shvarsman.coolinar.presentation.screens.recipe.list.RecipeListScreen
import com.shvarsman.coolinar.presentation.screens.recipe.suggested.SuggestedRecipesScreen
import com.shvarsman.coolinar.presentation.screens.recipe.view.RecipeViewScreen
import com.shvarsman.coolinar.presentation.screens.shoppinglist.ShoppingListScreen
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape
import com.shvarsman.coolinar.presentation.ui.theme.gradientStyle

private data class BottomItem(
    val destination: Destination,
    @androidx.annotation.StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

private val bottomItems = listOf(
    BottomItem(Destination.Home, R.string.nav_home, R.drawable.home),
    BottomItem(Destination.Fridge, R.string.nav_fridge, R.drawable.fridge),
    BottomItem(Destination.Recipes, R.string.nav_recipes, R.drawable.recipes),
    BottomItem(Destination.Profile, R.string.nav_profile, R.drawable.profile)
)

@Composable
fun AppNavGraph(showOnboarding: Boolean = false) {
    val rootNavController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = rootNavController,
            startDestination = if (showOnboarding) "onboarding" else "main_tabs_wrapper",
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

            composable("onboarding") {
                OnboardingScreen(
                    onFinished = {
                        rootNavController.navigate("main_tabs_wrapper") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
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
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
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
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                RecipeEditorScreen(
                    recipeId = recipeId,
                    onDone = { rootNavController.popBackStack() }
                )
            }

            composable(Destination.Cooking.route) {
                CookingScreen(
                    onBack = { rootNavController.popBackStack() },
                    onFinished = { rootNavController.popBackStack() }
                )
            }

            composable(Destination.CookSelection.route) {
                CookSelectionScreen(
                    onBack = { rootNavController.popBackStack() },
                    onNavigateToCooking = { rootNavController.navigate(Destination.Cooking.route) }
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

            composable(Destination.SuggestedRecipes.route) {
                SuggestedRecipesScreen(
                    onBack = { rootNavController.popBackStack() },
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
                    }
                )
            }

            composable(Destination.AllRecipesList.route) {
                AllRecipesListScreen(
                    onBack = { rootNavController.popBackStack() },
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
                    }
                )
            }

            composable(Destination.WeekMenu.route) {
                WeekMenuScreen(
                    onBack = { rootNavController.popBackStack() },
                    onCreateRecipe = {
                        rootNavController.navigate(
                            Destination.RecipeEditor.createRoute(Destination.RecipeEditor.NEW_RECIPE_ID)
                        )
                    },
                    onOpenCookSelection = { rootNavController.navigate(Destination.CookSelection.route) },
                    onViewRecipe = { recipeId ->
                        rootNavController.navigate(Destination.RecipeView.createRoute(recipeId))
                    }
                )
            }

            composable(Destination.Backup.route) {
                BackupScreen(onBack = { rootNavController.popBackStack() })
            }

            composable(Destination.ProfileSettings.route) {
                ProfileSettingsScreen(onBack = { rootNavController.popBackStack() })
            }

            composable(Destination.ShoppingList.route) {
                ShoppingListScreen(onBack = { rootNavController.popBackStack() })
            }

            composable(Destination.ProfileAuth.route) {
                AuthScreen(
                    onBack = { rootNavController.popBackStack() },
                    onAuthSuccess = { rootNavController.popBackStack() })
            }
        }
    }
}

@Composable
private fun MainTabsScreen(rootNavController: NavHostController) {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var hideBottomBar by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!hideBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(CornerShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                CornerShape
                            )
                            .gradientStyle()
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
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
                                    icon = {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(item.iconRes),
                                            contentDescription = stringResource(item.labelRes),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(stringResource(item.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.Transparent,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = childNavController,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onViewRecipe = { recipeId ->
                        rootNavController.navigate(Destination.RecipeView.createRoute(recipeId))
                    },
                    onOpenWeekMenu = { rootNavController.navigate(Destination.WeekMenu.route) },
                    onOpenFridge = {
                        childNavController.navigate(Destination.Fridge.route) {
                            popUpTo(childNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenShoppingList = { rootNavController.navigate(Destination.ShoppingList.route) },
                    onShowAllSuggested = { rootNavController.navigate(Destination.SuggestedRecipes.route) }
                )
            }

            composable(Destination.Fridge.route) {
                FridgeScreen(
                    onOpenCatalog = { rootNavController.navigate(Destination.ProductCatalog.route) },
                    onSelectionModeChange = { hideBottomBar = it }
                )
            }

            composable(Destination.Recipes.route) {
                RecipeListScreen(
                    onAddRecipe = {
                        rootNavController.navigate(
                            Destination.RecipeEditor.createRoute(
                                Destination.RecipeEditor.NEW_RECIPE_ID
                            )
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
                    onShowAllCategories = { rootNavController.navigate(Destination.AllCategories.route) },
                    onShowAllSuggested = { rootNavController.navigate(Destination.SuggestedRecipes.route) },
                    onShowAllRecipes = { rootNavController.navigate(Destination.AllRecipesList.route) }
                )
            }

            composable(Destination.Profile.route) {
                ProfileScreen(
                    onOpenBackup = { rootNavController.navigate(Destination.Backup.route) },
                    onOpenProfileSettings = { rootNavController.navigate(Destination.ProfileSettings.route) },
                    onOpenAuth = { rootNavController.navigate(Destination.ProfileAuth.route) }
                )
            }
        }
    }
}