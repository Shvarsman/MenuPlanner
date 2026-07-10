package com.shvarsman.menuplanner.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class RecipeCategory(val displayName: String, val icon: ImageVector) {
    SALADS_AND_APPETIZERS("Салаты и закуски", Icons.Filled.Eco),
    SOUPS("Супы", Icons.Filled.SoupKitchen),
    MAIN_MEAT("Вторые блюда. Мясо", Icons.Filled.LunchDining),
    MAIN_POULTRY("Вторые блюда. Птица", Icons.Filled.SetMeal),
    MAIN_FISH_SEAFOOD("Вторые блюда. Рыба и морепродукты", Icons.Filled.Water),
    MAIN_VEGETARIAN("Вторые блюда. Вегетарианские", Icons.Filled.Spa),
    SIDES_SAUCES("Гарниры, соусы, приправы", Icons.Filled.RiceBowl),
    BREAD_BAKING("Хлеб и выпечка", Icons.Filled.BakeryDining),
    DESSERTS("Десерты", Icons.Filled.Cake),
    DRINKS_ALCOHOL("Напитки. Алкоголь", Icons.Filled.WineBar),
    DRINKS_NON_ALCOHOL("Напитки. Без алкоголя", Icons.Filled.LocalCafe),
    OTHER("Другое", Icons.Filled.Restaurant)
}