package com.shvarsman.menuplanner.domain.model

/** Продукт/ингредиент, хранящийся в "холодильнике" пользователя. */
data class Product(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val unit: MeasureUnit,
    val quantity: Double
)

enum class MeasureUnit(val displayName: String) {
    GRAM("г"),
    KILOGRAM("кг"),
    MILLILITER("мл"),
    LITER("л"),
    PIECE("шт"),
    TABLESPOON("ст.л."),
    TEASPOON("ч.л."),
    PACK("уп")
}
