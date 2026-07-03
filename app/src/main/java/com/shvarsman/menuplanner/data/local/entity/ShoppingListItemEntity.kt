package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.MeasureUnit

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: MeasureUnit,
    val quantity: Double,
    val isChecked: Boolean = false,
    val fridgeProductId: Long? = null
)
