package com.shvarsman.menuplanner.domain.usecase.backup

import android.net.Uri
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.BackupResult
import com.shvarsman.menuplanner.domain.repository.BackupType
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