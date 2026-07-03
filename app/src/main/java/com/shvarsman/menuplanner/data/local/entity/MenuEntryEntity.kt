package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.MealType
import java.time.DayOfWeek

@Entity(tableName = "menu_entries")
data class MenuEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: Long
)
