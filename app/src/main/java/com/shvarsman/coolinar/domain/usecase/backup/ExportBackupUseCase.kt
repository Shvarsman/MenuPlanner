package com.shvarsman.coolinar.domain.usecase.backup

import android.net.Uri
import com.shvarsman.coolinar.domain.repository.BackupRepository
import com.shvarsman.coolinar.domain.repository.BackupResult
import com.shvarsman.coolinar.domain.repository.BackupType
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(
        destinationUri: Uri,
        type: BackupType,
        singleRecipeId: Long? = null
    ): BackupResult =
        repository.exportBackup(destinationUri, type, singleRecipeId)
}