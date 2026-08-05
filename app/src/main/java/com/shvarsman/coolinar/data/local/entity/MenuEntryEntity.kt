package com.shvarsman.coolinar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shvarsman.coolinar.domain.model.MealType
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(
    tableName = "menu_entries",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("weekStartDate")]
)
data class MenuEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: Long
)