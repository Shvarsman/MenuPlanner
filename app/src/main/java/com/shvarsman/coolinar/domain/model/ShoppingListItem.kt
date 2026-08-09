package com.shvarsman.coolinar.domain.model

import java.time.LocalDate

data class ShoppingListItem(
    val id: String = "",
    val product: Product,
    val unit: MeasureUnit,
    val quantity: Double,
    val isChecked: Boolean = false,
    val expirationDate: LocalDate? = null
)