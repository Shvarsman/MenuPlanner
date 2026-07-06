package com.shvarsman.menuplanner.data.repository

import com.shvarsman.menuplanner.data.local.dao.ProductDao
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import com.shvarsman.menuplanner.domain.model.Category
import com.shvarsman.menuplanner.domain.model.MeasureUnit
import com.shvarsman.menuplanner.domain.model.Product
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao
) : ProductRepository {

    override fun observeAllProducts(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getProduct(id: Long): Product? = dao.getById(id)?.toDomain()

    override suspend fun addProduct(product: Product): Long = dao.insert(product.toEntity())

    override suspend fun updateProduct(product: Product) = dao.update(product.toEntity())

    override suspend fun deleteProduct(id: Long) = dao.deleteById(id)

    override suspend fun findOrCreate(name: String, category: Category, defaultUnit: MeasureUnit): Product {
        dao.findByName(name)?.let { return it.toDomain() }
        val newId = dao.insert(ProductEntity(name = name, category = category, defaultUnit = defaultUnit))
        return Product(id = newId, name = name, category = category, defaultUnit = defaultUnit)
    }
}

private fun ProductEntity.toDomain() = Product(id = id, name = name, category = category, defaultUnit = defaultUnit)
private fun Product.toEntity() = ProductEntity(id = id, name = name, category = category, defaultUnit = defaultUnit)