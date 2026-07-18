package com.shvarsman.menuplanner.data.local.converter

import androidx.room.TypeConverter
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.CookingMethod
import com.shvarsman.menuplanner.domain.model.MealType
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.RecipeCategory
import com.shvarsman.menuplanner.domain.model.StepContentItem
import java.time.DayOfWeek

class Converters {

    @TypeConverter
    fun fromCategory(category: Category): String = category.toString()

    @TypeConverter
    fun toCategory(value: String): Category =
        Category.entries.first { it.name == value }

    @TypeConverter
    fun fromRecipeCategory(category: RecipeCategory): String = category.toString()

    @TypeConverter
    fun toRecipeCategory(value: String): RecipeCategory =
        RecipeCategory.entries.first { it.name == value }

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
    fun fromStepContentList(steps: List<StepContentItem>): String =
        steps.joinToString("\u241F") { item ->
            when (item) {
                is StepContentItem.Text -> "T${item.content}"
                is StepContentItem.Image -> "I${item.url}"
                is StepContentItem.Timer -> "M${item.minutes}"
            }
        }

    @TypeConverter
    fun toStepContentList(value: String): List<StepContentItem> {
        if (value.isBlank()) return emptyList()
        return value.split("\u241F").map { item ->
            when {
                item.startsWith("T") -> StepContentItem.Text(item.drop(1))
                item.startsWith("I") -> StepContentItem.Image(item.drop(1))
                item.startsWith("M") -> StepContentItem.Timer(item.drop(1).toIntOrNull() ?: 5)
                else -> StepContentItem.Text(item)
            }
        }
    }

    @TypeConverter
    fun fromCookingMethod(method: CookingMethod?): String? = method?.name

    @TypeConverter
    fun toCookingMethod(value: String?): CookingMethod? =
        value?.let { v -> CookingMethod.entries.firstOrNull { it.name == v } }
}