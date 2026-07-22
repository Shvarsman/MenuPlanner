package com.shvarsman.menuplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shvarsman.menuplanner.data.local.converter.Converters
import com.shvarsman.menuplanner.data.local.dao.FridgeItemDao
import com.shvarsman.menuplanner.data.local.dao.MenuDao
import com.shvarsman.menuplanner.data.local.dao.ProductDao
import com.shvarsman.menuplanner.data.local.dao.RecipeDao
import com.shvarsman.menuplanner.data.local.dao.ShoppingListDao
import com.shvarsman.menuplanner.data.local.entity.FridgeItemEntity
import com.shvarsman.menuplanner.data.local.entity.MenuEntryEntity
import com.shvarsman.menuplanner.data.local.entity.ProductEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeEntity
import com.shvarsman.menuplanner.data.local.entity.RecipeIngredientEntity
import com.shvarsman.menuplanner.data.local.entity.ShoppingListItemEntity

@Database(
    entities = [
        ProductEntity::class,
        FridgeItemEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MenuEntryEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun fridgeItemDao(): FridgeItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun menuDao(): MenuDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        const val DATABASE_NAME = "menu_planner.db"
    }
}