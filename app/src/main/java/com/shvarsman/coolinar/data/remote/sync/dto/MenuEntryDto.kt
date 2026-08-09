package com.shvarsman.coolinar.data.remote.sync.dto

data class MenuEntryDto(
    val weekStartEpochDay: Long = 0,
    val dayOfWeek: String = "",
    val mealType: String = "",
    val recipeId: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)