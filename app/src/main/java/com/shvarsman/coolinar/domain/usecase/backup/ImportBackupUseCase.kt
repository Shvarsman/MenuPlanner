package com.shvarsman.coolinar.domain.usecase.backup

import android.net.Uri
import com.shvarsman.coolinar.domain.repository.BackupRepository
import com.shvarsman.coolinar.domain.repository.BackupResult
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(sourceUri: Uri): BackupResult = repository.importBackup(sourceUri)
}