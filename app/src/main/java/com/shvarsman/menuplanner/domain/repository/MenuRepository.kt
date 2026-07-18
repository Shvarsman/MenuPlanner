package com.shvarsman.menuplanner.domain.repository

import com.shvarsman.menuplanner.domain.model.MenuEntry
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    /** Меню на текущую неделю (или на выбранный диапазон, при упрощении - на ближайшие 7 дней). */
    fun observeWeekMenu(): Flow<List<MenuEntry>>

    /**
     * Низкоуровневая вставка записи меню — без резервирования продуктов и
     * автогенерации списка покупок. Presentation-слой должен использовать
     * AssignRecipeToMenuUseCase, а не вызывать этот метод напрямую.
     */
    suspend fun addEntry(entry: MenuEntry): Long

    suspend fun removeEntry(id: Long)
}