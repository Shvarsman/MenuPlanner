package com.shvarsman.coolinar.data.remote.sync.dto

data class FridgeItemDto(
    val productId: String = "",
    val unit: String = "",
    val quantity: Double = 0.0,
    val expirationDateEpochDay: Long? = null,
    val favorite: Boolean = false,
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)