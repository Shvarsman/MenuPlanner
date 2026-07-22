package com.shvarsman.menuplanner.domain.model

import java.time.LocalDate

data class FridgeItem(
    val id: Long = 0,
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double,
    val expirationDate: LocalDate? = null,
    val isFavorite: Boolean = false
)