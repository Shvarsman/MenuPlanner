package com.shvarsman.menuplanner.domain.model

data class FridgeItem(
    val id: Long = 0,
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double
)