package com.shvarsman.coolinar.data.seed

import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit

internal object SeedProductMappers {

    // Русские названия категорий как формат CSV-файла — фиксированный,
    // не зависит от языка интерфейса (в отличие от Category.labelRes,
    // который теперь резолвится в Compose через stringResource). Если
    // измените текст в CSV, поменяйте и здесь.
    private val categoryByCsvName: Map<String, Category> = mapOf(
        "Фрукты" to Category.FRUITS,
        "Ягоды" to Category.BERRIES,
        "Цитрусовые" to Category.CITRUS,
        "Овощи" to Category.VEGETABLES,
        "Зелень и травы" to Category.HERBS,
        "Грибы" to Category.MUSHROOMS,
        "Мясо" to Category.MEAT,
        "Птица" to Category.POULTRY,
        "Субпродукты" to Category.OFFAL,
        "Рыба" to Category.FISH,
        "Морепродукты" to Category.SEAFOOD,
        "Консервы" to Category.CANNED,
        "Молочная продукция" to Category.DAIRY,
        "Сыры" to Category.CHEESE,
        "Яйца" to Category.EGGS,
        "Хлеб и выпечка" to Category.BREAD_BAKING,
        "Бакалея" to Category.GROCERY,
        "Соусы" to Category.SAUCES,
        "Крупы" to Category.GRAINS,
        "Бобовые" to Category.LEGUMES,
        "Макароны" to Category.PASTA,
        "Специи" to Category.SPICES,
        "Орехи и семена" to Category.NUTS_SEEDS,
        "Сухофрукты" to Category.DRIED_FRUITS,
        "Заморозка" to Category.FROZEN,
        "Кофе и чай" to Category.COFFEE_TEA,
        "Напитки безалкогольные" to Category.DRINKS_NON_ALCOHOL,
        "Напитки алкогольные" to Category.DRINKS_ALCOHOL,
        "Мёд и сладости" to Category.HONEY_SWEETS,
        "Снеки" to Category.SNACKS
    )

    private val unitMap: Map<String, MeasureUnit> = mapOf(
        "кг" to MeasureUnit.KILOGRAM,
        "г" to MeasureUnit.GRAM,
        "шт" to MeasureUnit.PIECE,
        "пучок" to MeasureUnit.PIECE,
        "л" to MeasureUnit.LITER,
        "мл" to MeasureUnit.MILLILITER,
        "банка" to MeasureUnit.PACK
    )

    fun mapCategory(raw: String): Category = categoryByCsvName[raw.trim()] ?: Category.GROCERY

    fun mapUnit(raw: String): MeasureUnit = unitMap[raw.trim()] ?: MeasureUnit.PIECE
}