package com.shvarsman.menuplanner.di

import com.shvarsman.menuplanner.data.repository.BackupRepositoryImpl
import com.shvarsman.menuplanner.data.repository.FridgeRepositoryImpl
import com.shvarsman.menuplanner.data.repository.MenuRepositoryImpl
import com.shvarsman.menuplanner.data.repository.ProductRepositoryImpl
import com.shvarsman.menuplanner.data.repository.RecipeRepositoryImpl
import com.shvarsman.menuplanner.data.repository.ShoppingListRepositoryImpl
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.FridgeRepository
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import com.shvarsman.menuplanner.domain.repository.ProductRepository
import com.shvarsman.menuplanner.domain.repository.RecipeRepository
import com.shvarsman.menuplanner.domain.repository.ShoppingListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindFridgeRepository(impl: FridgeRepositoryImpl): FridgeRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(impl: MenuRepositoryImpl): MenuRepository

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}