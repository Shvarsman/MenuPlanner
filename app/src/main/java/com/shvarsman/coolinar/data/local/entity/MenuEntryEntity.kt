package com.shvarsman.coolinar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shvarsman.coolinar.data.remote.sync.SyncableEntity
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
    @PrimaryKey override val id: String,
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val recipeId: String,
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity