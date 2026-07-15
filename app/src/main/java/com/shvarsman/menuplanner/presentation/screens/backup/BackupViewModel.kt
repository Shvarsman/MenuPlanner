package com.shvarsman.menuplanner.presentation.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.menuplanner.domain.repository.BackupResult
import com.shvarsman.menuplanner.domain.repository.BackupType
import com.shvarsman.menuplanner.domain.usecase.backup.ExportBackupUseCase
import com.shvarsman.menuplanner.domain.usecase.backup.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object InProgress : BackupUiState
    data class ExportSuccess(val result: BackupResult, val type: BackupType) : BackupUiState
    data class ImportSuccess(val result: BackupResult) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState

    fun onExport(destinationUri: Uri, type: BackupType, singleRecipeId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.InProgress
            try {
                val result = exportBackup(destinationUri, type, singleRecipeId)
                _uiState.value = BackupUiState.ExportSuccess(result, type)
            } catch (e: Exception) {
                _uiState.value =
                    BackupUiState.Error(e.message ?: "Не удалось создать резервную копию")
            }
        }
    }

    fun onImport(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.InProgress
            try {
                val result = importBackup(sourceUri)
                _uiState.value = BackupUiState.ImportSuccess(result)
            } catch (e: Exception) {
                _uiState.value =
                    BackupUiState.Error(e.message ?: "Не удалось восстановить резервную копию")
            }
        }
    }

    fun clearState() {
        _uiState.value = BackupUiState.Idle
    }
}