package com.shvarsman.menuplanner.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val icon: ImageVector) {
    VEGETABLES("Овощи",        Icons.Filled.Eco),
    FRUITS("Фрукты",           Icons.Filled.Spa),
    MEAT("Мясо",               Icons.Filled.LunchDining),
    FISH("Рыба",               Icons.Filled.Water),
    SPICES("Специи",           Icons.Filled.Grain),
    DRINKS("Вода / Напитки",   Icons.Filled.LocalCafe),
    GROCERY("Бакалея",         Icons.Filled.Inventory2),
    BAKERY("Хлеб",             Icons.Filled.BakeryDining),
    DAIRY("Молочка",           Icons.Filled.Icecream),
    SWEETS("Сладости",         Icons.Filled.Cake)
}