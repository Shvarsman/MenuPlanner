package com.shvarsman.coolinar.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class MenuEntry(
    val id: String = "",
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: String,
    val recipeTitle: String = "",
    val recipePhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)