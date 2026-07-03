package com.shvarsman.menuplanner.domain.model

import java.time.DayOfWeek

/** Запись меню: какой рецепт назначен на день недели и приём пищи. */
data class MenuEntry(
    val id: Long = 0,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: Long,
    val recipeTitle: String = "",
    val recipePhotoUri: String? = null
)
