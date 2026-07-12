package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.FridgeItemDao
import com.shvarsman.menuplanner.data.local.dao.FridgeItemWithProduct
import com.shvarsman.menuplanner.data.local.entity.FridgeItemEntity
import com.shvarsman.menuplanner.domain.model.FridgeItem
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FridgeRepositoryImpl @Inject constructor(
    private val dao: FridgeItemDao
) : FridgeRepository {

    override fun observeItems(): Flow<List<FridgeItem>> =
        dao.observeAllWithProduct().map { list ->
            list.map { it.toDomain() }.sortedBy { it.product.name }
        }

    override suspend fun getItem(id: Long): FridgeItem? = dao.getByIdWithProduct(id)?.toDomain()

    override suspend fun addItem(item: FridgeItem): Long = dao.insert(item.toEntity())

    override suspend fun updateItem(item: FridgeItem) = dao.update(item.toEntity())

    override suspend fun deleteItem(id: Long) = dao.deleteById(id)

    override suspend fun decreaseQuantity(id: Long, amount: Double) = dao.decreaseQuantity(id, amount)
}

private fun FridgeItemWithProduct.toDomain() = FridgeItem(
    id = item.id,
    product = Product(
        id = product.id, name = product.name, category = product.category,
        defaultUnit = product.defaultUnit, iconKey = product.iconKey,
        isDefault = product.isDefault, isToTaste = product.isToTaste
    ),
    unit = item.unit,
    quantity = item.quantity
)

private fun FridgeItem.toEntity() = FridgeItemEntity(id = id, productId = product.id, unit = unit, quantity = quantity)