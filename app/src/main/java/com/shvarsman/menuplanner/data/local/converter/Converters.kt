package com.shvarsman.menuplanner.data.local.converter


import androidx.room.TypeConverter
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import java.time.DayOfWeek

class Converters {


    @TypeConverter
    fun fromMeasureUnit(unit: MeasureUnit): String = unit.toString()

    @TypeConverter
    fun toMeasureUnit(value: String): MeasureUnit =
        MeasureUnit.entries.first { it.name == value }

    @TypeConverter
    fun fromMealType(type: MealType): String = type.toString()

    @TypeConverter
    fun toMealType(value: String): MealType =
        MealType.entries.first { it.name == value }

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek): String = day.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)

    @TypeConverter
    fun fromStepList(steps: List<String>): String = steps.joinToString("§")

    @TypeConverter
    fun toStepList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("§")
}
