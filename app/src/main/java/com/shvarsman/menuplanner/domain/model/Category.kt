package com.shvarsman.menuplanner.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val icon: ImageVector) {
    FRUITS("Фрукты", Icons.Filled.Spa),
    BERRIES("Ягоды", Icons.Filled.Grain),
    CITRUS("Цитрусовые", Icons.Filled.WbSunny),
    VEGETABLES("Овощи", Icons.Filled.Eco),
    HERBS("Зелень и травы", Icons.Filled.Grass),
    MUSHROOMS("Грибы", Icons.Filled.Park),
    MEAT("Мясо", Icons.Filled.LunchDining),
    POULTRY("Птица", Icons.Filled.SetMeal),
    OFFAL("Субпродукты", Icons.Filled.Restaurant),
    FISH("Рыба", Icons.Filled.Water),
    SEAFOOD("Морепродукты", Icons.Filled.SetMeal),
    CANNED("Консервы", Icons.Filled.Inventory2),
    DAIRY("Молочная продукция", Icons.Filled.Icecream),
    CHEESE("Сыры", Icons.Filled.Circle),
    EGGS("Яйца", Icons.Filled.Egg),
    BREAD_BAKING("Хлеб и выпечка", Icons.Filled.BakeryDining),
    GROCERY("Бакалея", Icons.Filled.ShoppingBasket),
    SAUCES("Соусы", Icons.Filled.WaterDrop),
    GRAINS("Крупы", Icons.Filled.Grain),
    LEGUMES("Бобовые", Icons.Filled.Grain),
    PASTA("Макароны", Icons.Filled.RiceBowl),
    SPICES("Специи", Icons.Filled.Grain),
    NUTS_SEEDS("Орехи и семена", Icons.Filled.Park),
    DRIED_FRUITS("Сухофрукты", Icons.Filled.WbSunny),
    FROZEN("Заморозка", Icons.Filled.AcUnit),
    COFFEE_TEA("Кофе и чай", Icons.Filled.Coffee),
    DRINKS_NON_ALCOHOL("Напитки безалкогольные", Icons.Filled.LocalCafe),
    DRINKS_ALCOHOL("Напитки алкогольные", Icons.Filled.WineBar),
    HONEY_SWEETS("Мёд и сладости", Icons.Filled.Cake),
    SNACKS("Снеки", Icons.Filled.Fastfood)
}