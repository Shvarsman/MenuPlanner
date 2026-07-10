package com.shvarsman.menuplanner.domain.usecase.backup

import android.net.Uri
import com.shvarsman.menuplanner.domain.repository.BackupRepository
import com.shvarsman.menuplanner.domain.repository.BackupResult
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(sourceUri: Uri): BackupResult = repository.importBackup(sourceUri)
}