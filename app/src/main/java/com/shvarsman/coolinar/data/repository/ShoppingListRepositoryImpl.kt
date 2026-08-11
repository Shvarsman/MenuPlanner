package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.ShoppingListDao
import com.shvarsman.coolinar.data.local.dao.ShoppingListItemWithProduct
import com.shvarsman.coolinar.data.local.entity.ShoppingListItemEntity
import com.shvarsman.coolinar.data.remote.sync.ShoppingListSyncEngine
import com.shvarsman.coolinar.data.remote.sync.SyncScope
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.model.ShoppingListItem
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val dao: ShoppingListDao,
    private val syncEngine: ShoppingListSyncEngine,
    private val authRepository: AuthRepository,
    private val syncScope: SyncScope
) : ShoppingListRepository {

    override fun observeItems(): Flow<List<ShoppingListItem>> =
        dao.observeAllWithProduct().map { list ->
            list.map { it.toDomain() }
                .sortedWith(
                    compareBy({ it.isChecked }, { it.product.sortName() })
                )
        }

    override suspend fun addItem(item: ShoppingListItem): String {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val entity = item.toEntity().copy(id = id, updatedAt = System.currentTimeMillis())
        dao.insert(entity)
        pushIfSignedIn(entity)
        return id
    }

    override suspend fun updateItem(item: ShoppingListItem) {
        val entity = item.toEntity().copy(updatedAt = System.currentTimeMillis())
        dao.update(entity)
        pushIfSignedIn(entity)
    }

    override suspend fun removeItem(id: String) {
        val now = System.currentTimeMillis()
        dao.softDeleteById(id, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        val now = System.currentTimeMillis()
        dao.setChecked(id, checked, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    override suspend fun clearChecked() {
        val now = System.currentTimeMillis()
        val checkedIds = dao.getCheckedIds()
        dao.softDeleteChecked(now)
        checkedIds.forEach { id -> dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) } }
    }

    private fun pushIfSignedIn(entity: ShoppingListItemEntity) {
        val uid = authRepository.currentUserId ?: return
        syncScope.scope.launch {
            runCatching { syncEngine.push(uid, entity) }
        }
    }
}

private fun ShoppingListItemWithProduct.toDomain() = ShoppingListItem(
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
    isChecked = item.isChecked,
    expirationDate = item.expirationDate
)

private fun ShoppingListItem.toEntity() = ShoppingListItemEntity(
    id = id,
    productId = product.id,
    unit = unit,
    quantity = quantity,
    isChecked = isChecked,
    expirationDate = expirationDate
)