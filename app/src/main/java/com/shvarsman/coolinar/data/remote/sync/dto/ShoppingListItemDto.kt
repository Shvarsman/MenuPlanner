package com.shvarsman.coolinar.data.remote.sync.dto

data class ShoppingListItemDto(
    val productId: String = "",
    val unit: String = "",
    val quantity: Double = 0.0,
    val checked: Boolean = false,
    val expirationDateEpochDay: Long? = null,
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)