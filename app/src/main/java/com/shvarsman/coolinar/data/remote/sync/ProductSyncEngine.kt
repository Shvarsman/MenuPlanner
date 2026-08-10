package com.shvarsman.coolinar.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.shvarsman.coolinar.data.local.dao.ProductDao
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import com.shvarsman.coolinar.data.remote.sync.dto.ProductDto
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductSyncEngine @Inject constructor(
    firestore: FirebaseFirestore,
    private val dao: ProductDao,
    syncScope: SyncScope
) : FirestoreSyncEngine<ProductEntity, ProductDto>(firestore, "products", ProductDto::class.java, syncScope) {

    override suspend fun ProductEntity.toDto() = ProductDto(
        name = name,
        category = category.name,
        defaultUnit = defaultUnit.name,
        iconKey = iconKey,
        default = isDefault,
        toTaste = isToTaste,
        alwaysAvailable = isAlwaysAvailable,
        updatedAt = updatedAt,
        deleted = isDeleted
    )

    override fun ProductDto.toLocal(id: String) = ProductEntity(
        id = id,
        name = name,
        category = Category.entries.first { it.name == category },
        defaultUnit = MeasureUnit.entries.first { it.name == defaultUnit },
        iconKey = iconKey,
        isDefault = default,
        isToTaste = toTaste,
        isAlwaysAvailable = alwaysAvailable,
        updatedAt = updatedAt,
        isDeleted = deleted
    )

    override suspend fun upsertLocal(items: List<Pair<ProductEntity, ProductDto>>) {
        items.forEach { (entity, _) -> dao.insert(entity) }
    }

    override suspend fun getAllLocalIncludingDeleted(): List<ProductEntity> =
        dao.getAllIncludingDeleted()

    override suspend fun getLocalById(id: String): ProductEntity? =
        dao.getByIdIncludingDeleted(id)
}