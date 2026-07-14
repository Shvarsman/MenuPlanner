package com.shvarsman.menuplanner.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.vector.ImageVector
import com.shvarsman.menuplanner.domain.model.Category

val Category.icon: ImageVector
    get() = when (this) {
        Category.FRUITS -> Icons.Filled.Spa
        Category.BERRIES -> Icons.Filled.Grain
        Category.CITRUS -> Icons.Filled.WbSunny
        Category.VEGETABLES -> Icons.Filled.Eco
        Category.HERBS -> Icons.Filled.Grass
        Category.MUSHROOMS -> Icons.Filled.Park
        Category.MEAT -> Icons.Filled.LunchDining
        Category.POULTRY -> Icons.Filled.SetMeal
        Category.OFFAL -> Icons.Filled.Restaurant
        Category.FISH -> Icons.Filled.Water
        Category.SEAFOOD -> Icons.Filled.SetMeal
        Category.CANNED -> Icons.Filled.Inventory2
        Category.DAIRY -> Icons.Filled.Icecream
        Category.CHEESE -> Icons.Filled.Circle
        Category.EGGS -> Icons.Filled.Egg
        Category.BREAD_BAKING -> Icons.Filled.BakeryDining
        Category.GROCERY -> Icons.Filled.ShoppingBasket
        Category.SAUCES -> Icons.Filled.WaterDrop
        Category.GRAINS -> Icons.Filled.Grain
        Category.LEGUMES -> Icons.Filled.Grain
        Category.PASTA -> Icons.Filled.RiceBowl
        Category.SPICES -> Icons.Filled.Grain
        Category.NUTS_SEEDS -> Icons.Filled.Park
        Category.DRIED_FRUITS -> Icons.Filled.WbSunny
        Category.FROZEN -> Icons.Filled.AcUnit
        Category.COFFEE_TEA -> Icons.Filled.Coffee
        Category.DRINKS_NON_ALCOHOL -> Icons.Filled.LocalCafe
        Category.DRINKS_ALCOHOL -> Icons.Filled.WineBar
        Category.HONEY_SWEETS -> Icons.Filled.Cake
        Category.SNACKS -> Icons.Filled.Fastfood
    }
