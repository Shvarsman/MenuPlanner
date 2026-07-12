package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.ShoppingListDao
import com.shvarsman.menuplanner.data.local.dao.ShoppingListItemWithProduct
import com.shvarsman.menuplanner.data.local.entity.ShoppingListItemEntity
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.model.ShoppingListItem
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val dao: ShoppingListDao
) : ShoppingListRepository {

    override fun observeItems(): Flow<List<ShoppingListItem>> =
        dao.observeAllWithProduct().map { list ->
            list.map { it.toDomain() }
                .sortedWith(
                    compareBy({ it.isChecked }, { it.product.name })
                )
        }

    override suspend fun addItem(item: ShoppingListItem): Long = dao.insert(item.toEntity())

    override suspend fun updateItem(item: ShoppingListItem) = dao.update(item.toEntity())

    override suspend fun removeItem(id: Long) = dao.deleteById(id)

    override suspend fun setChecked(id: Long, checked: Boolean) = dao.setChecked(id, checked)

    override suspend fun clearChecked() = dao.clearChecked()
}

private fun ShoppingListItemWithProduct.toDomain() = ShoppingListItem(
    id = item.id,
    product = Product(
        id = product.id,
        name = product.name,
        category = product.category,
        defaultUnit = product.defaultUnit,
        iconKey = product.iconKey,
        isDefault = product.isDefault
    ),
    unit = item.unit,
    quantity = item.quantity,
    isChecked = item.isChecked
)
private fun ShoppingListItem.toEntity() = ShoppingListItemEntity(
    id = id,
    productId = product.id,
    unit = unit,
    quantity = quantity,
    isChecked = isChecked
)