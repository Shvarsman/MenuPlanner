package com.shvarsman.menuplanner.domain.repository

import android.net.Uri

enum class BackupType { FULL, RECIPES_ONLY, SINGLE_RECIPE }

data class BackupResult(
    val fridgeItemsCount: Int = 0,
    val shoppingItemsCount: Int = 0,
    val menuEntriesCount: Int = 0,
    val recipesCount: Int = 0
)

interface BackupRepository {
    /** Экспортирует резервную копию согласно [type]. Для [BackupType.SINGLE_RECIPE]
     * обязателен [singleRecipeId]. */
    suspend fun exportBackup(destinationUri: Uri, type: BackupType, singleRecipeId: Long? = null): BackupResult

    /** Восстанавливает данные из архива. Тип резервной копии определяется
     * автоматически по содержимому файла — вызывающей стороне не нужно его знать. */
    suspend fun importBackup(sourceUri: Uri): BackupResult
}