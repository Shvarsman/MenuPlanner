package com.shvarsman.menuplanner.domain.model

import java.time.LocalDate

data class ShoppingListItem(
    val id: Long = 0,
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double,
    val isChecked: Boolean = false,
    val expirationDate: LocalDate? = null
)