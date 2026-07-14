package com.shvarsman.menuplanner.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.vector.ImageVector
import com.shvarsman.menuplanner.domain.model.RecipeCategory

val RecipeCategory.icon: ImageVector
    get() = when (this) {
        RecipeCategory.SALADS_AND_APPETIZERS -> Icons.Filled.Eco
        RecipeCategory.SOUPS -> Icons.Filled.SoupKitchen
        RecipeCategory.MAIN_MEAT -> Icons.Filled.LunchDining
        RecipeCategory.MAIN_POULTRY -> Icons.Filled.SetMeal
        RecipeCategory.MAIN_FISH_SEAFOOD -> Icons.Filled.Water
        RecipeCategory.MAIN_VEGETARIAN -> Icons.Filled.Spa
        RecipeCategory.SIDES_SAUCES -> Icons.Filled.RiceBowl
        RecipeCategory.BREAD_BAKING -> Icons.Filled.BakeryDining
        RecipeCategory.DESSERTS -> Icons.Filled.Cake
        RecipeCategory.DRINKS_ALCOHOL -> Icons.Filled.WineBar
        RecipeCategory.DRINKS_NON_ALCOHOL -> Icons.Filled.LocalCafe
        RecipeCategory.OTHER -> Icons.Filled.Restaurant
    }
