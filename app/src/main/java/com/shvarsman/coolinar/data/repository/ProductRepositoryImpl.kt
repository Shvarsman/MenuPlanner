package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.ProductDao
import com.shvarsman.coolinar.data.local.entity.ProductEntity
import com.shvarsman.coolinar.data.remote.sync.ProductSyncEngine
import com.shvarsman.coolinar.data.remote.sync.SyncScope
import com.shvarsman.coolinar.domain.model.Category
import com.shvarsman.coolinar.domain.model.MeasureUnit
import com.shvarsman.coolinar.domain.model.Product
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val syncEngine: ProductSyncEngine,
    private val authRepository: AuthRepository,
    private val syncScope: SyncScope
) : ProductRepository {

    override fun observeAllProducts(): Flow<List<Product>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() }.sortedBy { it.sortName().lowercase() } }
            .flowOn(Dispatchers.Default)

    override suspend fun getProduct(id: String): Product? = dao.getById(id)?.toDomain()

    override suspend fun addProduct(product: Product): String {
        val id = product.id.ifBlank { UUID.randomUUID().toString() }
        val entity = product.toEntity().copy(id = id, updatedAt = System.currentTimeMillis())
        dao.insert(entity)
        pushIfSignedIn(entity)
        return id
    }

    override suspend fun updateProduct(product: Product) {
        val entity = product.toEntity().copy(updatedAt = System.currentTimeMillis())
        dao.update(entity)
        pushIfSignedIn(entity)
    }

    override suspend fun deleteProduct(id: String) {
        val now = System.currentTimeMillis()
        dao.softDeleteById(id, now)
        dao.getByIdIncludingDeleted(id)?.let { pushIfSignedIn(it) }
    }

    override suspend fun findOrCreate(
        name: String, category: Category, defaultUnit: MeasureUnit,
        isToTaste: Boolean, isAlwaysAvailable: Boolean
    ): Product {
        dao.findByName(name)?.let { return it.toDomain() }
        val newId = UUID.randomUUID().toString()
        val entity = ProductEntity(
            id = newId, name = name, nameEn = name, category = category, defaultUnit = defaultUnit,
            isToTaste = isToTaste, isAlwaysAvailable = isAlwaysAvailable
        )
        dao.insert(entity)
        pushIfSignedIn(entity)
        return Product(
            id = newId, name = name, nameEn = name, category = category, defaultUnit = defaultUnit,
            isToTaste = isToTaste, isAlwaysAvailable = isAlwaysAvailable
        )
    }

    override suspend fun countUsages(productId: String): Int = dao.countUsages(productId)

    private fun pushIfSignedIn(entity: ProductEntity) {
        val uid = authRepository.currentUserId ?: return
        syncScope.scope.launch {
            runCatching { syncEngine.push(uid, entity) }
        }
    }
}

private fun ProductEntity.toDomain() = Product(
    id = id, name = name, nameEn = nameEn, category = category, defaultUnit = defaultUnit,
    iconKey = iconKey, isDefault = isDefault, isToTaste = isToTaste,
    isAlwaysAvailable = isAlwaysAvailable
)

private fun Product.toEntity() = ProductEntity(
    id = id, name = name, nameEn = nameEn, category = category, defaultUnit = defaultUnit,
    iconKey = iconKey, isDefault = isDefault, isToTaste = isToTaste,
    isAlwaysAvailable = isAlwaysAvailable
)