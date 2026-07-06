package com.shvarsman.menuplanner.di

import android.content.Context
import androidx.room.Room
import com.shvarsman.menuplanner.data.local.AppDatabase
import com.shvarsman.menuplanner.data.local.dao.FridgeItemDao
import com.shvarsman.menuplanner.data.local.dao.MenuDao
import com.shvarsman.menuplanner.data.local.dao.ProductDao
import com.shvarsman.menuplanner.data.local.dao.RecipeDao
import com.shvarsman.menuplanner.data.local.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideMenuDao(db: AppDatabase): MenuDao = db.menuDao()

    @Provides
    fun provideShoppingListDao(db: AppDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    fun provideFridgeItemDao(db: AppDatabase): FridgeItemDao = db.fridgeItemDao()
}
