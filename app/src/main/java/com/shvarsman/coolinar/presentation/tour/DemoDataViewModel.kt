package com.shvarsman.coolinar.presentation.tour

import androidx.lifecycle.ViewModel
import com.shvarsman.coolinar.domain.usecase.backup.ImportDemoBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

@HiltViewModel
class DemoDataViewModel @Inject constructor(
    private val importDemoBackup: ImportDemoBackupUseCase,
    private val demoDataRepository: com.shvarsman.coolinar.data.repository.DemoDataRepositoryImpl
) : ViewModel() {

    private var restoreJob: CompletableDeferred<Unit>? = null

    suspend fun restoreDemoDataIfNeeded() {
        val existing = restoreJob
        if (existing != null) {
            existing.await()
            return
        }
        val deferred = CompletableDeferred<Unit>()
        restoreJob = deferred
        try {
            importDemoBackup()
        } catch (t: Throwable) {
            android.util.Log.e("DemoData", "Failed to restore demo backup for tour", t)
        } finally {
            deferred.complete(Unit)
        }
    }

    suspend fun deleteAllDemoData() {
        demoDataRepository.purgeAll()
    }

    suspend fun deleteAllDemoDataExceptRecipes() {
        demoDataRepository.purgeAllExceptRecipes()
    }
}