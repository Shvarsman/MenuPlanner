package com.shvarsman.coolinar.di

import com.shvarsman.coolinar.data.repository.BackupRepositoryImpl
import com.shvarsman.coolinar.data.repository.FirebaseAuthRepositoryImpl
import com.shvarsman.coolinar.data.repository.FridgeRepositoryImpl
import com.shvarsman.coolinar.data.repository.MenuRepositoryImpl
import com.shvarsman.coolinar.data.repository.OnboardingRepositoryImpl
import com.shvarsman.coolinar.data.repository.ProductRepositoryImpl
import com.shvarsman.coolinar.data.repository.RecipeRepositoryImpl
import com.shvarsman.coolinar.data.repository.RoomTransactionRunner
import com.shvarsman.coolinar.data.repository.ShoppingListRepositoryImpl
import com.shvarsman.coolinar.data.repository.UserPreferencesRepositoryImpl
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.BackupRepository
import com.shvarsman.coolinar.domain.repository.FridgeRepository
import com.shvarsman.coolinar.domain.repository.MenuRepository
import com.shvarsman.coolinar.domain.repository.OnboardingRepository
import com.shvarsman.coolinar.domain.repository.ProductRepository
import com.shvarsman.coolinar.domain.repository.RecipeRepository
import com.shvarsman.coolinar.domain.repository.ShoppingListRepository
import com.shvarsman.coolinar.domain.repository.TransactionRunner
import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
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

    @Binds
    @Singleton
    abstract fun bindTransactionRunner(impl: RoomTransactionRunner): TransactionRunner

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository
}