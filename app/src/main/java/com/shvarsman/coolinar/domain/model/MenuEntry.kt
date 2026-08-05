package com.shvarsman.coolinar.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class MenuEntry(
    val id: Long = 0,
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: Long,
    val recipeTitle: String = "",
    val recipePhotoUri: String? = null
)