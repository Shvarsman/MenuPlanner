package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import java.time.LocalDate

@Entity(
    tableName = "fridge_items",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class FridgeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val unit: MeasureUnit,
    val quantity: Double,
    val expirationDate: LocalDate? = null,
    val isFavorite: Boolean = false
)