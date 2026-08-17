package com.shvarsman.coolinar.domain.repository

import android.net.Uri

enum class BackupType { FULL, RECIPES_ONLY, SINGLE_RECIPE }

data class BackupResult(
    val fridgeItemsCount: Int = 0,
    val shoppingItemsCount: Int = 0,
    val menuEntriesCount: Int = 0,
    val recipesCount: Int = 0
)

interface BackupRepository {
    suspend fun exportBackup(destinationUri: Uri, type: BackupType, singleRecipeId: String? = null): BackupResult
    suspend fun importBackup(sourceUri: Uri): BackupResult

    /** Восстанавливает демо-данные из файла в assets (для guided-тура) —
     * та же логика импорта, что и importBackup(Uri), только источник не
     * выбирается пользователем через SAF, а зашит в приложение. */
    suspend fun importDemoBackup(assetFileName: String): BackupResult
}