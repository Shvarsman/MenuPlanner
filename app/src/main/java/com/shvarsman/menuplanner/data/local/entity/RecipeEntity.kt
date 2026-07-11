package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.StepContentItem

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val cookingMethod: CookingMethod?,
    val cookingTimeMinutes: Int?,
    val steps: List<StepContentItem>
)