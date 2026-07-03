package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.ProductDao
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FridgeRepositoryImpl @Inject constructor(
    private val dao: ProductDao
) : FridgeRepository {

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProduct(id: Long): Product? =
        dao.getById(id)?.toDomain()

    override suspend fun addProduct(product: Product): Long =
        dao.insert(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        dao.update(product.toEntity())

    override suspend fun deleteProduct(id: Long) =
        dao.deleteById(id)

    override suspend fun decreaseQuantity(id: Long, amount: Double) =
        dao.decreaseQuantity(id, amount)
}

private fun ProductEntity.toDomain() = Product(
    id = id, name = name, iconKey = iconKey, unit = unit, quantity = quantity
)

private fun Product.toEntity() = ProductEntity(
    id = id, name = name, iconKey = iconKey, unit = unit, quantity = quantity
)
