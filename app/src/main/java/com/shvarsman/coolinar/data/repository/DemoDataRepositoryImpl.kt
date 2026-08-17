package com.shvarsman.coolinar.data.repository

import com.shvarsman.coolinar.data.local.dao.FridgeItemDao
import com.shvarsman.coolinar.data.local.dao.MenuDao
import com.shvarsman.coolinar.data.local.dao.ProductDao
import com.shvarsman.coolinar.data.local.dao.RecipeDao
import com.shvarsman.coolinar.data.local.dao.ShoppingListDao
import javax.inject.Inject

/**
 * Физическая очистка демо-данных после guided-тура. НЕ использует обычные
 * репозитории/юзкейсы удаления — те все идут через soft-delete + tombstone
 * для синхронизации с Firestore, что для демо-данных не нужно и оставляло
 * бы вечный "мусор" (isDeleted = true) в базе. Работает с DAO напрямую —
 * осознанное отступление от чистой архитектуры для узкой инфраструктурной
 * задачи, не бизнес-операции.
 */
class DemoDataRepositoryImpl @Inject constructor(
    private val fridgeItemDao: FridgeItemDao,
    private val shoppingListDao: ShoppingListDao,
    private val menuDao: MenuDao,
    private val recipeDao: RecipeDao,
    private val productDao: ProductDao
) {

    /** Удаляет всё: холодильник, список покупок, меню, все рецепты и все
     * пользовательские (не сидовые) продукты. */
    suspend fun purgeAll() {
        fridgeItemDao.deleteAllHard()
        shoppingListDao.deleteAllHard()
        menuDao.deleteAllHard()

        val allRecipeIds = recipeDao.getAllIdsIncludingDeleted()
        if (allRecipeIds.isNotEmpty()) recipeDao.deleteByIdsHard(allRecipeIds)

        val userProducts = productDao.getAllUserCreated()
        if (userProducts.isNotEmpty()) productDao.deleteByIdsHard(userProducts.map { it.id })
    }

    /** Удаляет холодильник, список покупок, меню и неиспользуемые
     * пользовательские продукты — но НЕ рецепты и НЕ продукты, которые в
     * оставленных рецептах используются как ингредиенты (иначе рецепты
     * остались бы без части ингредиентов из-за ForeignKey CASCADE). */
    suspend fun purgeAllExceptRecipes() {
        fridgeItemDao.deleteAllHard()
        shoppingListDao.deleteAllHard()
        menuDao.deleteAllHard()

        // countUsages() после очистки холодильника/списка покупок отражает
        // только использование в оставшихся recipe_ingredients — то есть
        // ровно то, что нужно: продукт удаляем, только если он нигде в
        // рецептах, которые мы сохраняем, не участвует.
        val userProducts = productDao.getAllUserCreated()
        val unusedIds = userProducts
            .filter { productDao.countUsages(it.id) == 0 }
            .map { it.id }
        if (unusedIds.isNotEmpty()) productDao.deleteByIdsHard(unusedIds)
    }
}