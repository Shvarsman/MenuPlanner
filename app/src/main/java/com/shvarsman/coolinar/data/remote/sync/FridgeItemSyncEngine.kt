package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.dao.FridgeItemDao
import com.shvarsman.coolinar.data.local.entity.FridgeItemEntity
import com.shvarsman.coolinar.data.remote.sync.dto.FridgeItemDto
import com.shvarsman.coolinar.domain.model.MeasureUnit
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeItemSyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: FridgeItemDao,
    syncScope: SyncScope
) : FirestoreSyncEngine<FridgeItemEntity, FridgeItemDto>(
    firestore, "fridgeItems", FridgeItemDto::class.java, syncScope
) {
    override suspend fun FridgeItemEntity.toDto() = FridgeItemDto(
        productId = productId,
        unit = unit.name,
        quantity = quantity,
        expirationDateEpochDay = expirationDate?.toEpochDay(),
        favorite = isFavorite,
        updatedAt = updatedAt,
        deleted = isDeleted
    )

    override fun FridgeItemDto.toLocal(id: String) = FridgeItemEntity(
        id = id,
        productId = productId,
        unit = MeasureUnit.entries.first { it.name == unit },
        quantity = quantity,
        expirationDate = expirationDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        isFavorite = favorite,
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<FridgeItemEntity, FridgeItemDto>>) {
        items.forEach { (entity, _) -> dao.insert(entity) }
    }

    override suspend fun getAllLocalIncludingDeleted(): List<FridgeItemEntity> =
        dao.getAllIncludingDeleted()
}