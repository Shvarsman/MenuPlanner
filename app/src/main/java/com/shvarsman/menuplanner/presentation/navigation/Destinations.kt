package com.shvarsman.menuplanner.presentation.navigation

sealed class Destination(val route: String) {
    object Menu : Destination("menu")
    object Fridge : Destination("fridge")
    object Recipes : Destination("recipes")
    object RecipeEditor : Destination("recipe_editor/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe_editor/$recipeId"
        const val NEW_RECIPE_ID = 0L
    }
    object ShoppingList : Destination("shopping_list")

    object ProductCatalog : Destination("product_catalog")
}
