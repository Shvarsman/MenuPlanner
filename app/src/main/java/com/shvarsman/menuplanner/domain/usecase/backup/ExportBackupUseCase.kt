package com.shvarsman.menuplanner.domain.usecase.backup

import android.net.Uri
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.BackupResult
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(destinationUri: Uri): BackupResult = repository.exportBackup(destinationUri)
}