package com.shvarsman.menuplanner.domain.model

data class ShoppingListItem(
    val id: Long = 0,
    val name: String,
    val unit: MeasureUnit,
    val quantity: Double,
    val isChecked: Boolean = false,
    val fridgeProductId: Long? = null
)
