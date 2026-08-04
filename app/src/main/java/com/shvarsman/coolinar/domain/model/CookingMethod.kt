package com.shvarsman.coolinar.domain.model

enum class CookingMethod(val displayName: String) {
    BOILING("Варка"),
    STEAMING("Варка на пару"),
    FRYING("Жарка"),
    DEEP_FRYING("Жарка во фритюре"),
    STEWING("Тушение"),
    OVEN_BAKING("Запекание в духовке"),
    BAKING_IN_POTS("Запекание в горшочках"),
    GRILLING("Гриль"),
    BARBECUE("Мангал / Открытый огонь"),
    SMOKING("Копчение"),
    SIMMERING("Томление"),
    CONFIT("Конфи"),
    SOUS_VIDE("Су-вид (Sous vide)"),
    BLANCHING("Бланширование"),
    FLAMBEING("Фламбирование"),
    MICROWAVE("Микроволновка (СВЧ)"),
    SLOW_COOKER("Мультиварка"),
    AIR_FRYER("Аэрогриль"),
    BREAD_MAKER("Хлебопечка"),
    TOASTER_WAFFLE("Тостер / Вафельница"),
    NO_COOKING("Без приготовления (сборка, нарезка)"),
    MARINATING("Маринование"),
    SALTING_FERMENTING("Соление / Ферментация"),
    CURING_DRYING("Вяление / Сушка"),
    FREEZING("Заморозка")
}