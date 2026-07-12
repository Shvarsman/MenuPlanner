package com.shvarsman.menuplanner.data.seed

import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit

data class SeedProductRow(
    val name: String,
    val category: Category,
    val unit: MeasureUnit
)

/** Разбирает CSV вида "продукт;категория;ед_измерения" (первая строка — заголовок). */
object SeedProductCsvParser {
    fun parse(csvText: String): List<SeedProductRow> {
        return csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .drop(1) // пропускаем заголовок
            .mapNotNull { line ->
                val parts = line.split(";")
                if (parts.size < 3) return@mapNotNull null
                val name = parts[0].trim()
                if (name.isBlank()) return@mapNotNull null
                SeedProductRow(
                    name = name,
                    category = SeedProductMappers.mapCategory(parts[1]),
                    unit = SeedProductMappers.mapUnit(parts[2])
                )
            }
            .toList()
    }
}