package com.shvarsman.coolinar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.coolinar.domain.model.CookingMethod
import com.shvarsman.coolinar.domain.model.RecipeCategory
import com.shvarsman.coolinar.domain.model.RecipeDifficulty
import com.shvarsman.coolinar.domain.model.StepContentItem

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: RecipeCategory,
    val photoUri: String?,
    val cookingMethod: CookingMethod?,
    val cookingTimeMinutes: Int?,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val description: String = "",
    val steps: List<StepContentItem>,
    val stepCount: Int
)