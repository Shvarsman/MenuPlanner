package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.dao.ShoppingListDao
import com.shvarsman.coolinar.data.local.entity.ShoppingListItemEntity
import com.shvarsman.coolinar.data.remote.sync.dto.ShoppingListItemDto
import com.shvarsman.coolinar.domain.model.MeasureUnit
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListSyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: ShoppingListDao
) : FirestoreSyncEngine<ShoppingListItemEntity, ShoppingListItemDto>(
    firestore, "shoppingListItems", ShoppingListItemDto::class.java
) {
    override suspend fun ShoppingListItemEntity.toDto() = ShoppingListItemDto(
        productId = productId,
        unit = unit.name,
        quantity = quantity,
        checked = isChecked,
        expirationDateEpochDay = expirationDate?.toEpochDay(),
        updatedAt = updatedAt,
        deleted = isDeleted
    )

    override fun ShoppingListItemDto.toLocal(id: String) = ShoppingListItemEntity(
        id = id,
        productId = productId,
        unit = MeasureUnit.entries.first { it.name == unit },
        quantity = quantity,
        isChecked = checked,
        expirationDate = expirationDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<ShoppingListItemEntity, ShoppingListItemDto>>) {
        items.forEach { (entity, _) -> dao.insert(entity) }
    }

    override suspend fun getAllLocalIncludingDeleted(): List<ShoppingListItemEntity> =
        dao.getAllIncludingDeleted()
}