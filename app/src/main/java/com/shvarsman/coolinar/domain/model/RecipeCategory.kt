package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R

enum class RecipeCategory(@StringRes val labelRes: Int) {
    SALADS_AND_APPETIZERS(R.string.recipe_category_salads_and_appetizers),
    SOUPS(R.string.recipe_category_soups),
    MAIN_MEAT(R.string.recipe_category_main_meat),
    MAIN_POULTRY(R.string.recipe_category_main_poultry),
    MAIN_FISH_SEAFOOD(R.string.recipe_category_main_fish_seafood),
    MAIN_VEGETARIAN(R.string.recipe_category_main_vegetarian),
    SIDES_SAUCES(R.string.recipe_category_sides_sauces),
    BREAD_BAKING(R.string.recipe_category_bread_baking),
    DESSERTS(R.string.recipe_category_desserts),
    DRINKS_ALCOHOL(R.string.recipe_category_drinks_alcohol),
    DRINKS_NON_ALCOHOL(R.string.recipe_category_drinks_non_alcohol),
    OTHER(R.string.recipe_category_other)
}