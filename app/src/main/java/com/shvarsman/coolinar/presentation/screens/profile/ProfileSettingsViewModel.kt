package com.shvarsman.coolinar.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthException
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val displayName: StateFlow<String?> = userPreferencesRepository.displayName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSavingName = MutableStateFlow(false)
    val isSavingName: StateFlow<Boolean> = _isSavingName

    private val _nameSaved = MutableStateFlow(false)
    val nameSaved: StateFlow<Boolean> = _nameSaved

    fun saveDisplayName(name: String) {
        if (_isSavingName.value) return
        viewModelScope.launch {
            _isSavingName.value = true
            userPreferencesRepository.setDisplayName(name)
            _isSavingName.value = false
            _nameSaved.value = true
        }
    }

    private val _isChangingPassword = MutableStateFlow(false)
    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword

    private val _passwordErrorRes = MutableStateFlow<Int?>(null)
    val passwordErrorRes: StateFlow<Int?> = _passwordErrorRes

    private val _passwordChanged = MutableStateFlow(false)
    val passwordChanged: StateFlow<Boolean> = _passwordChanged

    fun clearPasswordError() {
        _passwordErrorRes.value = null
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (_isChangingPassword.value) return
        viewModelScope.launch {
            _isChangingPassword.value = true
            _passwordErrorRes.value = null
            _passwordChanged.value = false
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess { _passwordChanged.value = true }
                .onFailure { _passwordErrorRes.value = it.toErrorRes() }
            _isChangingPassword.value = false
        }
    }
}

private fun Throwable.toErrorRes(): Int = when (this) {
    is AuthException.InvalidEmail -> R.string.profile_error_invalid_email
    is AuthException.WeakPassword -> R.string.profile_error_weak_password
    is AuthException.WrongCredentials -> R.string.profile_error_wrong_credentials
    is AuthException.EmailAlreadyInUse -> R.string.profile_error_email_in_use
    is AuthException.NetworkError -> R.string.profile_error_network
    else -> R.string.profile_error_generic
}