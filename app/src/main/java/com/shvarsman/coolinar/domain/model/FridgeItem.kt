package com.shvarsman.coolinar.domain.model

import java.time.LocalDate

data class FridgeItem(
    val id: String = "",
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double,
    val expirationDate: LocalDate? = null,
    val isFavorite: Boolean = false
)