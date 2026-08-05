package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.MenuEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MenuRepository {
    fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntry>>

    suspend fun getEntry(id: Long): MenuEntry?

    suspend fun addEntry(entry: MenuEntry): Long

    suspend fun removeEntry(id: Long)
}