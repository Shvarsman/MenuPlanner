package com.shvarsman.coolinar.presentation.ui.icons

import androidx.annotation.DrawableRes
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.RecipeCategory

@get:DrawableRes
val RecipeCategory.imageRes: Int
    get() = when (this) {
        RecipeCategory.SALADS_AND_APPETIZERS -> R.drawable.category_salads_snacks
        RecipeCategory.SOUPS -> R.drawable.category_soups
        RecipeCategory.MAIN_MEAT -> R.drawable.category_meat
        RecipeCategory.MAIN_POULTRY -> R.drawable.category_poultry
        RecipeCategory.MAIN_FISH_SEAFOOD -> R.drawable.category_fish_seafood
        RecipeCategory.MAIN_VEGETARIAN -> R.drawable.category_vegetarian
        RecipeCategory.SIDES_SAUCES -> R.drawable.category_sides_sauces
        RecipeCategory.BREAD_BAKING -> R.drawable.category_bread_bakery
        RecipeCategory.DESSERTS -> R.drawable.category_desserts
        RecipeCategory.DRINKS_ALCOHOL -> R.drawable.category_drinks_alcohol
        RecipeCategory.DRINKS_NON_ALCOHOL -> R.drawable.category_drinks_nonalcohol
        RecipeCategory.OTHER -> R.drawable.category_other
    }