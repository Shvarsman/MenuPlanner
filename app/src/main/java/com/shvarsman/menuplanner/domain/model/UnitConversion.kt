package com.shvarsman.menuplanner.domain.model

/**
 * Конвертация количеств между совместимыми единицами измерения одной
 * физической величины (масса или объём). Единицы разных величин
 * (например, PIECE и GRAM) несовместимы — конвертация возвращает null.
 */
object UnitConversion {

    private val massToGrams: Map<MeasureUnit, Double> = mapOf(
        MeasureUnit.GRAM to 1.0,
        MeasureUnit.KILOGRAM to 1000.0
    )

    private val volumeToMilliliters: Map<MeasureUnit, Double> = mapOf(
        MeasureUnit.MILLILITER to 1.0,
        MeasureUnit.LITER to 1000.0
    )

    /** Конвертирует [amount] из [from] в [to]. Возвращает null, если единицы
     * относятся к разным физическим величинам (например, кг и шт) и
     * конвертация невозможна. */
    fun convert(amount: Double, from: MeasureUnit, to: MeasureUnit): Double? {
        if (from == to) return amount

        massToGrams[from]?.let { fromFactor ->
            massToGrams[to]?.let { toFactor -> return amount * fromFactor / toFactor }
        }
        volumeToMilliliters[from]?.let { fromFactor ->
            volumeToMilliliters[to]?.let { toFactor -> return amount * fromFactor / toFactor }
        }
        return null
    }

    /** Каноническая единица для суммирования количеств одной физической
     * величины из разных рецептов/записей: граммы для массы, миллилитры для
     * объёма, иначе — сама единица (несовместимые величины не суммируются
     * между собой). */
    fun canonicalUnit(unit: MeasureUnit): MeasureUnit = when {
        massToGrams.containsKey(unit) -> MeasureUnit.GRAM
        volumeToMilliliters.containsKey(unit) -> MeasureUnit.MILLILITER
        else -> unit
    }
}