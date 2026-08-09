package com.shvarsman.coolinar.data.remote.sync.dto

data class ProductDto(
    val name: String = "",
    val category: String = "",
    val defaultUnit: String = "",
    val iconKey: String = "",
    // Без префикса "is" — геттер isXxx() у Kotlin-boolean заставляет Firestore
    // писать поле под именем "xxx" (обрезает "is"), но при ЧТЕНИИ обратно оно
    // ищет поле, буквально названное "xxx", и не находит "isXxx" — тихо теряет
    // значение (см. "No setter/field for xxx found" в логе). Простое имя без
    // "is" читается/пишется симметрично.
    val default: Boolean = false,
    val toTaste: Boolean = false,
    val alwaysAvailable: Boolean = false,
    val updatedAt: Long = 0,
    val deleted: Boolean = false
)