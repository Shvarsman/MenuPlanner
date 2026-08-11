package com.shvarsman.coolinar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.coolinar.data.remote.sync.SyncableEntity
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey override val id: String,
    val name: String,
    val nameEn: String,
    val category: Category,
    val defaultUnit: MeasureUnit,
    val iconKey: String = Product.DEFAULT_ICON_KEY,
    val isDefault: Boolean = false,
    val isToTaste: Boolean = false,
    val isAlwaysAvailable: Boolean = false,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false
) : SyncableEntity