package com.shvarsman.menuplanner.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val defaultUnit: MeasureUnit,
    val iconKey: String = DEFAULT_ICON_KEY,
    val isDefault: Boolean = false
) {
    companion object {
        const val DEFAULT_ICON_KEY = "default"
    }
}

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
