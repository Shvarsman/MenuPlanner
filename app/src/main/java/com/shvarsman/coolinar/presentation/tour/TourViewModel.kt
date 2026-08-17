package com.shvarsman.coolinar.presentation.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TourViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow<TourStep?>(null)
    val currentStep: StateFlow<TourStep?> = _currentStep.asStateFlow()

    private val _pendingTabSelection = MutableStateFlow<String?>(null)
    val pendingTabSelection: StateFlow<String?> = _pendingTabSelection.asStateFlow()

    /** Вызывается один раз после restore демо-данных, если isTourPending == true. */
    /** Одноразовое чтение актуального значения флага — без побочных эффектов
     * (не потребляет флаг). Вызывать перед restore демо-данных, чтобы решить,
     * нужен ли тур вообще. */

    /** Вызывается один раз после restore демо-данных, если isPendingOnce() == true. */
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    suspend fun isPendingOnce(): Boolean = onboardingRepository.isTourPending.first()

    /** Проверяет, нужен ли тур, восстанавливает демо-данные (если нужен) и
     * только потом переводит isReady в true — MainTabsScreen не показывает
     * контент, пока это не произойдёт, чтобы избежать вспышки "пустой экран
     * → данные появились → маскот появился". */
    fun prepare(restoreDemoData: suspend () -> Unit) {
        viewModelScope.launch {
            if (isPendingOnce()) {
                restoreDemoData()
                onboardingRepository.consumeTourPending()
                _currentStep.value = TourStep.HOME
            }
            _isReady.value = true
        }
    }

    fun next() {
        _currentStep.value = _currentStep.value?.next
    }

    private val _showFinishDialog = MutableStateFlow(false)
    val showFinishDialog: StateFlow<Boolean> = _showFinishDialog.asStateFlow()

    /** Вызывается с последнего шага тура (Profile) — тур скрывается,
     * показывается финальный диалог "готов начать?" с выбором судьбы
     * демо-данных (удалить всё / оставить только рецепты). */
    fun finish() {
        _currentStep.value = null
        _showFinishDialog.value = true
    }

    private val _isProcessingFinish = MutableStateFlow(false)
    val isProcessingFinish: StateFlow<Boolean> = _isProcessingFinish.asStateFlow()

    fun dismissFinishDialog() {
        _showFinishDialog.value = false
    }

    /** onAction — сам вызов use case удаления (передаётся снаружи, чтобы
     * TourViewModel не был напрямую завязан на репозитории данных). */
    fun confirmFinish(onAction: suspend () -> Unit) {
        if (_isProcessingFinish.value) return
        viewModelScope.launch {
            _isProcessingFinish.value = true
            try {
                onAction()
            } finally {
                _isProcessingFinish.value = false
                _showFinishDialog.value = false
            }
        }
    }

    fun consumeTabSelection() {
        _pendingTabSelection.value = null
    }

    fun requestTabSelection(route: String) {
        _pendingTabSelection.value = route
    }
}