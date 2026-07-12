package com.shvarsman.menuplanner.data.seed

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit

data class SeedProductRow(
    val name: String,
    val category: Category,
    val unit: MeasureUnit,
    val isToTaste: Boolean
)

/** Разбирает CSV вида "продукт;категория;ед_измерения;по_вкусу" (4-я колонка
 * необязательна, отсутствие или "0"/пусто трактуется как false). */
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
                SeedProductRow(
                    name = name,
                    category = SeedProductMappers.mapCategory(parts[1]),
                    unit = SeedProductMappers.mapUnit(parts[2]),
                    isToTaste = isToTaste
                )
            }
            .toList()
    }
}