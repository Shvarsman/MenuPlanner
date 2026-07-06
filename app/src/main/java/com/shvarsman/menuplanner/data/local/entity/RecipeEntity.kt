package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.StepContentItem

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val photoUri: String?,
    val steps: List<StepContentItem>
)