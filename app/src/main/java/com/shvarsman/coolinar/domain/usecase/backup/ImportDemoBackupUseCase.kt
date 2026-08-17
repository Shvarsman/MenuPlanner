package com.shvarsman.coolinar.domain.usecase.backup

import com.shvarsman.coolinar.domain.repository.BackupRepository
import com.shvarsman.coolinar.domain.repository.BackupResult
import javax.inject.Inject

class ImportDemoBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(assetFileName: String = "demo_backup.zip"): BackupResult =
        repository.importDemoBackup(assetFileName)
}