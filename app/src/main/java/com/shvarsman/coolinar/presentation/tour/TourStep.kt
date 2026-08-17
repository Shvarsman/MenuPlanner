package com.shvarsman.coolinar.presentation.tour

enum class TourStep {
    HOME, FRIDGE, SHOPPING_LIST, RECIPES, WEEK_MENU, PROFILE;

    val next: TourStep? get() = entries.getOrNull(ordinal + 1)
}