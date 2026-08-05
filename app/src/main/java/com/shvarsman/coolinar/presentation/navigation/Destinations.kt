package com.shvarsman.coolinar.presentation.navigation

import com.shvarsman.coolinar.domain.model.RecipeCategory

sealed class Destination(val route: String) {

    object Home : Destination("menu")

    object Fridge : Destination("fridge")

    object Recipes : Destination("recipes")

    object RecipeEditor : Destination("recipe_editor/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe_editor/$recipeId"
        const val NEW_RECIPE_ID = 0L
    }

    object ShoppingList : Destination("shopping_list")

    object ProductCatalog : Destination("product_catalog")

    object Cooking : Destination("cooking/{recipeId}/{menuEntryId}") {
        fun createRoute(recipeId: Long, menuEntryId: Long) = "cooking/$recipeId/$menuEntryId"
    }

    object RecipeView : Destination("recipe_view/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe_view/$recipeId"
    }

    object RecipeCategoryList : Destination("recipe_category/{category}") {
        fun createRoute(category: RecipeCategory) = "recipe_category/${category.name}"
    }

    object Backup : Destination("backup")

    object AllCategories : Destination("all_categories")

    object SuggestedRecipes : Destination("suggested_recipes")

    object AllRecipesList : Destination("all_recipes_list")

    object WeekMenu : Destination("week_menu")

    object Profile : Destination("profile")

    object ProfileSettings : Destination("profile_settings")
}