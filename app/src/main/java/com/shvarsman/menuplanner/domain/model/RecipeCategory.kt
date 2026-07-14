package com.shvarsman.menuplanner.domain.model

enum class RecipeCategory(val displayName: String) {
    SALADS_AND_APPETIZERS("Салаты и закуски"),
    SOUPS("Супы"),
    MAIN_MEAT("Вторые блюда. Мясо"),
    MAIN_POULTRY("Вторые блюда. Птица"),
    MAIN_FISH_SEAFOOD("Вторые блюда. Рыба и морепродукты"),
    MAIN_VEGETARIAN("Вторые блюда. Вегетарианские"),
    SIDES_SAUCES("Гарниры, соусы, приправы"),
    BREAD_BAKING("Хлеб и выпечка"),
    DESSERTS("Десерты"),
    DRINKS_ALCOHOL("Напитки. Алкоголь"),
    DRINKS_NON_ALCOHOL("Напитки. Без алкоголя"),
    OTHER("Другое")
}
