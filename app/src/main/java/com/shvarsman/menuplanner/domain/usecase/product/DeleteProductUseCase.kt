package com.shvarsman.menuplanner.domain.usecase.product

import com.shvarsman.menuplanner.domain.repository.ProductRepository
import javax.inject.Inject

/** Продукт используется в рецептах/холодильнике/списке покупок — для удаления
 * требуется явное подтверждение (force = true), т.к. связанные записи будут утеряны. */
class ProductInUseException(val usagesCount: Int) : Exception(
    "Продукт используется в $usagesCount местах — удаление сотрёт эти данные"
)

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Long, force: Boolean = false) {
        val product = repository.getProduct(productId)
        require(product == null || !product.isDefault) { "Стандартные продукты нельзя удалить" }

        if (!force) {
            val usages = repository.countUsages(productId)
            if (usages > 0) throw ProductInUseException(usages)
        }

        repository.deleteProduct(productId)
    }
}