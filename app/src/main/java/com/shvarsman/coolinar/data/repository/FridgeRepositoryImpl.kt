package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.FridgeItemDao
import com.shvarsman.coolinar.data.local.dao.FridgeItemWithProduct
import com.shvarsman.coolinar.data.local.entity.FridgeItemEntity
import com.shvarsman.coolinar.data.remote.sync.FridgeItemSyncEngine
import com.shvarsman.coolinar.data.remote.sync.SyncScope
import com.shvarsman.coolinar.domain.model.FridgeItem
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class FridgeRepositoryImpl @Inject constructor(
    private val dao: FridgeItemDao,
    private val syncEngine: FridgeItemSyncEngine,
    private val authRepository: AuthRepository,
    private val syncScope: SyncScope
) : FridgeRepository {

    override fun observeItems(): Flow<List<FridgeItem>> =
        dao.observeAllWithProduct()
            .map { list -> list.map { it.toDomain() }.sortedBy { it.product.sortName() } }
            .flowOn(Dispatchers.Default)

    override suspend fun getItem(id: String): FridgeItem? = dao.getByIdWithProduct(id)?.toDomain()

    override suspend fun addItem(item: FridgeItem): String {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val entity = item.toEntity().copy(id = id, updatedAt = System.currentTimeMillis())
        dao.insert(entity)
        pushIfSignedIn(entity)
        return id
    }

    override suspend fun updateItem(item: FridgeItem) {
        val entity = item.toEntity().copy(updatedAt = System.currentTimeMillis())
        dao.update(entity)
        pushIfSignedIn(entity)
    }

    override suspend fun deleteItem(id: String) {
        val now = System.currentTimeMillis()
        dao.softDeleteById(id, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    override suspend fun decreaseQuantity(id: String, amount: Double) {
        val now = System.currentTimeMillis()
        dao.decreaseQuantity(id, amount, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    private fun pushIfSignedIn(entity: FridgeItemEntity) {
        val uid = authRepository.currentUserId ?: return
        syncScope.scope.launch {
            runCatching { syncEngine.push(uid, entity) }
        }
    }
}

private fun FridgeItemWithProduct.toDomain() = FridgeItem(
    id = item.id,
    product = Product(
        id = product.id,
        name = product.name,
        nameEn = product.nameEn,
        category = product.category,
        defaultUnit = product.defaultUnit,
        iconKey = product.iconKey,
        isDefault = product.isDefault,
        isToTaste = product.isToTaste
    ),
    unit = item.unit,
    quantity = item.quantity,
    expirationDate = item.expirationDate,
    isFavorite = item.isFavorite
)

private fun FridgeItem.toEntity() = FridgeItemEntity(
    id = id,
    productId = product.id,
    unit = unit,
    quantity = quantity,
    expirationDate = expirationDate,
    isFavorite = isFavorite
)