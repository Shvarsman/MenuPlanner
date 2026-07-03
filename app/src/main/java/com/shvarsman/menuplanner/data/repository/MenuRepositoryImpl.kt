package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.MenuDao
import com.shvarsman.menuplanner.data.local.dao.MenuEntryWithRecipe
import com.shvarsman.menuplanner.data.local.entity.MenuEntryEntity
import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val dao: MenuDao
) : MenuRepository {

    override fun observeWeekMenu(): Flow<List<MenuEntry>> =
        dao.observeWeekMenu().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(entry: MenuEntry): Long =
        dao.insert(MenuEntryEntity(dayOfWeek = entry.dayOfWeek, mealType = entry.mealType, recipeId = entry.recipeId))

    override suspend fun removeEntry(id: Long) = dao.deleteById(id)
}

private fun MenuEntryWithRecipe.toDomain() = MenuEntry(
    id = entry.id,
    dayOfWeek = entry.dayOfWeek,
    mealType = entry.mealType,
    recipeId = entry.recipeId,
    recipeTitle = recipe.title,
    recipePhotoUri = recipe.photoUri
)
