package com.shvarsman.coolinar.presentation.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.data.local.ImageFileManager
import com.shvarsman.coolinar.data.remote.storage.RemoteImageUploader
import com.shvarsman.coolinar.domain.model.AuthException
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteImageUploader: RemoteImageUploader,
    private val imageFileManager: ImageFileManager
) : ViewModel() {

    val displayName: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.displayName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val avatarUrl: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.photoUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSavingName = MutableStateFlow(false)
    val isSavingName: StateFlow<Boolean> = _isSavingName

    private val _nameSaved = MutableStateFlow(false)
    val nameSaved: StateFlow<Boolean> = _nameSaved

    private val _nameErrorRes = MutableStateFlow<Int?>(null)
    val nameErrorRes: StateFlow<Int?> = _nameErrorRes

    private val _isUpdatingAvatar = MutableStateFlow(false)
    val isUpdatingAvatar: StateFlow<Boolean> = _isUpdatingAvatar

    private val _avatarErrorRes = MutableStateFlow<Int?>(null)
    val avatarErrorRes: StateFlow<Int?> = _avatarErrorRes

    fun saveDisplayName(name: String) {
        if (_isSavingName.value) return
        viewModelScope.launch {
            _isSavingName.value = true
            _nameSaved.value = false
            _nameErrorRes.value = null
            authRepository.updateDisplayName(name)
                .onSuccess { _nameSaved.value = true }
                .onFailure { _nameErrorRes.value = R.string.profile_error_generic }
            _isSavingName.value = false
        }
    }

    fun updateAvatar(sourceUri: Uri) {
        if (_isUpdatingAvatar.value) return
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _isUpdatingAvatar.value = true
            _avatarErrorRes.value = null
            runCatching {
                val localUri = imageFileManager.persistImage(sourceUri)
                val remoteUrl = remoteImageUploader.uploadAvatar(uid, localUri)
                imageFileManager.deleteImage(localUri)
                remoteUrl
            }.mapCatching { remoteUrl ->
                authRepository.updatePhotoUrl(remoteUrl).getOrThrow()
            }.onFailure {
                _avatarErrorRes.value = R.string.profile_error_generic
            }
            _isUpdatingAvatar.value = false
        }
    }

    fun removeAvatar() {
        if (_isUpdatingAvatar.value) return
        val currentUrl = avatarUrl.value ?: return
        viewModelScope.launch {
            _isUpdatingAvatar.value = true
            _avatarErrorRes.value = null
            authRepository.updatePhotoUrl(null)
                .onSuccess { remoteImageUploader.delete(currentUrl) }
                .onFailure { _avatarErrorRes.value = R.string.profile_error_generic }
            _isUpdatingAvatar.value = false
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