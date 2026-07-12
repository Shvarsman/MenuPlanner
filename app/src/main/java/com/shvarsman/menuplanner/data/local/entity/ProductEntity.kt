package com.shvarsman.menuplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: Category,
    val defaultUnit: MeasureUnit,
    val iconKey: String = Product.DEFAULT_ICON_KEY,
    val isDefault: Boolean = false
)