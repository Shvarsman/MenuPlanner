package com.shvarsman.menuplanner.data.seed

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product

data class SeedProductRow(
    val name: String,
    val category: Category,
    val unit: MeasureUnit,
    val isToTaste: Boolean,
    val iconKey: String
)

/** Разбирает CSV вида "продукт;категория;ед_измерения;по_вкусу;иконка"
 * (4-я и 5-я колонки необязательны: по_вкусу — "0"/пусто трактуется как false,
 * иконка — пустая строка трактуется как Product.DEFAULT_ICON_KEY, тогда в UI
 * используется фолбэк на иконку категории, см. ProductIcon.kt). */
object SeedProductCsvParser {
    fun parse(csvText: String): List<SeedProductRow> {
        return csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .drop(1)
            .mapNotNull { line ->
                val parts = line.split(";")
                if (parts.size < 3) return@mapNotNull null
                val name = parts[0].trim()
                if (name.isBlank()) return@mapNotNull null
                val isToTaste = parts.getOrNull(3)?.trim() == "1"
                val iconKey = parts.getOrNull(4)?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: Product.DEFAULT_ICON_KEY
                SeedProductRow(
                    name = name,
                    category = SeedProductMappers.mapCategory(parts[1]),
                    unit = SeedProductMappers.mapUnit(parts[2]),
                    isToTaste = isToTaste,
                    iconKey = iconKey
                )
            }
            .toList()
    }
}