package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.MenuDao
import com.shvarsman.coolinar.data.local.dao.MenuEntryWithRecipe
import com.shvarsman.coolinar.data.local.entity.MenuEntryEntity
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val dao: MenuDao
) : MenuRepository {

    override fun observeWeekMenu(weekStart: LocalDate): Flow<List<MenuEntry>> =
        dao.observeWeekMenu(weekStart).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getEntry(id: Long): MenuEntry? = dao.getByIdWithRecipe(id)?.toDomain()

    override suspend fun addEntry(entry: MenuEntry): Long =
        dao.insert(
            MenuEntryEntity(
                weekStartDate = entry.weekStartDate,
                dayOfWeek = entry.dayOfWeek,
                mealType = entry.mealType,
                recipeId = entry.recipeId
            )
        )

    override suspend fun removeEntry(id: Long) = dao.deleteById(id)
}

private fun MenuEntryWithRecipe.toDomain() = MenuEntry(
    id = entry.id,
    weekStartDate = entry.weekStartDate,
    dayOfWeek = entry.dayOfWeek,
    mealType = entry.mealType,
    recipeId = entry.recipeId,
    recipeTitle = recipe.title,
    recipePhotoUri = recipe.photoUri
)
