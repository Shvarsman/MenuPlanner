package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.dao.MenuDao
import com.shvarsman.coolinar.data.local.entity.MenuEntryEntity
import com.shvarsman.coolinar.data.remote.sync.dto.MenuEntryDto
import com.shvarsman.coolinar.domain.model.MealType
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuEntrySyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: MenuDao,
    syncScope: SyncScope
) : FirestoreSyncEngine<MenuEntryEntity, MenuEntryDto>(
    firestore, "menuEntries", MenuEntryDto::class.java, syncScope
) {
    override suspend fun MenuEntryEntity.toDto() = MenuEntryDto(
        weekStartEpochDay = weekStartDate.toEpochDay(),
        dayOfWeek = dayOfWeek.name,
        mealType = mealType.name,
        recipeId = recipeId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deleted = isDeleted
    )

    override fun MenuEntryDto.toLocal(id: String) = MenuEntryEntity(
        id = id,
        weekStartDate = LocalDate.ofEpochDay(weekStartEpochDay),
        dayOfWeek = DayOfWeek.valueOf(dayOfWeek),
        mealType = MealType.entries.first { it.name == mealType },
        recipeId = recipeId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<MenuEntryEntity, MenuEntryDto>>) {
        items.forEach { (entity, _) -> dao.insert(entity) }
    }

    override suspend fun getAllLocalIncludingDeleted(): List<MenuEntryEntity> =
        dao.getAllIncludingDeleted()

    override suspend fun getLocalById(id: String): MenuEntryEntity? =
        dao.getByIdIncludingDeleted(id)
}