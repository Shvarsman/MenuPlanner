package com.shvarsman.coolinar.domain.repository

interface DemoDataRepository {
    suspend fun purgeAll()
    suspend fun purgeAllExceptRecipes()
}
