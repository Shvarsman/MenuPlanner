package com.shvarsman.menuplanner.data.seed

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit

internal object SeedProductMappers {

    private val categoryByDisplayName: Map<String, Category> =
        Category.entries.associateBy { it.displayName }

    private val unitMap: Map<String, MeasureUnit> = mapOf(
        "кг" to MeasureUnit.KILOGRAM,
        "г" to MeasureUnit.GRAM,
        "шт" to MeasureUnit.PIECE,
        "пучок" to MeasureUnit.PIECE,
        "л" to MeasureUnit.LITER,
        "мл" to MeasureUnit.MILLILITER,
        "банка" to MeasureUnit.PACK
    )

    fun mapCategory(raw: String): Category = categoryByDisplayName[raw.trim()] ?: Category.GROCERY

    fun mapUnit(raw: String): MeasureUnit = unitMap[raw.trim()] ?: MeasureUnit.PIECE
}