package com.shvarsman.coolinar.domain.model

import androidx.annotation.StringRes
import com.shvarsman.coolinar.R

enum class Category(@StringRes val labelRes: Int) {
    FRUITS(R.string.category_fruits),
    BERRIES(R.string.category_berries),
    CITRUS(R.string.category_citrus),
    VEGETABLES(R.string.category_vegetables),
    HERBS(R.string.category_herbs),
    MUSHROOMS(R.string.category_mushrooms),
    MEAT(R.string.category_meat),
    POULTRY(R.string.category_poultry),
    OFFAL(R.string.category_offal),
    FISH(R.string.category_fish),
    SEAFOOD(R.string.category_seafood),
    CANNED(R.string.category_canned),
    DAIRY(R.string.category_dairy),
    CHEESE(R.string.category_cheese),
    EGGS(R.string.category_eggs),
    BREAD_BAKING(R.string.category_bread_baking),
    GROCERY(R.string.category_grocery),
    SAUCES(R.string.category_sauces),
    GRAINS(R.string.category_grains),
    LEGUMES(R.string.category_legumes),
    PASTA(R.string.category_pasta),
    SPICES(R.string.category_spices),
    NUTS_SEEDS(R.string.category_nuts_seeds),
    DRIED_FRUITS(R.string.category_dried_fruits),
    FROZEN(R.string.category_frozen),
    COFFEE_TEA(R.string.category_coffee_tea),
    DRINKS_NON_ALCOHOL(R.string.category_drinks_non_alcohol),
    DRINKS_ALCOHOL(R.string.category_drinks_alcohol),
    HONEY_SWEETS(R.string.category_honey_sweets),
    SNACKS(R.string.category_snacks)
}