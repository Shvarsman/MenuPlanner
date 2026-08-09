package com.shvarsman.coolinar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shvarsman.coolinar.data.remote.sync.SyncableEntity
import com.shvarsman.coolinar.domain.model.MeasureUnit
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
    @PrimaryKey override val id: String,
    val productId: String,
    val unit: MeasureUnit,
    val quantity: Double,
    val expirationDate: LocalDate? = null,
    val isFavorite: Boolean = false,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity