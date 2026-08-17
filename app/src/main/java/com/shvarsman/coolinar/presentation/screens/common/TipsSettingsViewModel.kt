package com.shvarsman.coolinar.presentation.screens.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.repository.TipsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TipsSettingsViewModel @Inject constructor(
    private val tipsRepository: TipsRepository
) : ViewModel() {
    val tipsEnabled: StateFlow<Boolean> = tipsRepository.tipsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setTipsEnabled(enabled: Boolean) {
        viewModelScope.launch { tipsRepository.setTipsEnabled(enabled) }
    }
}