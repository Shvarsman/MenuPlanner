package com.shvarsman.coolinar.presentation.screens.cooking

import javax.inject.Inject
import javax.inject.Singleton

/** Одно блюдо к готовке: рецепт и все записи меню, которые эта готовка закрывает
 * (больше одной — если один и тот же рецепт выбран сразу на несколько приёмов пищи). */
data class CookingDish(
    val recipeId: String,
    val menuEntryIds: List<String>
)

/**
 * In-memory хранилище выбора блюд между CookSelectionScreen и CookingScreen —
 * по аналогии с SyncScope. Роут "cooking" не принимает аргументов, поэтому список
 * выбранных блюд передаётся не через NavGraph, а через этот синглтон.
 */
@Singleton
class CookingSessionHolder @Inject constructor() {

    private var pendingDishes: List<CookingDish> = emptyList()

    fun set(dishes: List<CookingDish>) {
        pendingDishes = dishes
    }

    fun consume(): List<CookingDish> {
        val result = pendingDishes
        pendingDishes = emptyList()
        return result
    }
}