package com.shvarsman.coolinar.domain.repository

import com.shvarsman.coolinar.domain.model.MenuEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MenuRepository {
    fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntry>>
    suspend fun getEntry(id: String): MenuEntry?
    suspend fun addEntry(entry: MenuEntry): String
    suspend fun removeEntry(id: String)
    suspend fun restoreEntry(id: String)
}