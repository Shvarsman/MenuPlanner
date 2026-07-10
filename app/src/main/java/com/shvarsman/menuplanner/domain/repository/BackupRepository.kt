package com.shvarsman.menuplanner.domain.repository

import android.net.Uri

data class BackupResult(val productsCount: Int, val recipesCount: Int)

interface BackupRepository {
    /** Экспортирует все продукты и рецепты в zip-архив по указанному URI. */
    suspend fun exportBackup(destinationUri: Uri): BackupResult

    /** Восстанавливает продукты и рецепты из zip-архива. Продукты сопоставляются
     * с существующими по названию (регистр не важен); рецепты всегда добавляются
     * как новые записи — повторный импорт одного и того же архива создаст дубли рецептов. */
    suspend fun importBackup(sourceUri: Uri): BackupResult
}