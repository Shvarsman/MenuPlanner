package com.shvarsman.coolinar.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthException
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.repository.AuthRepository
import com.shvarsman.coolinar.domain.repository.OnboardingRepository
import com.shvarsman.coolinar.domain.usecase.auth.SignUpWithEmailUseCase
import com.shvarsman.coolinar.presentation.screens.profile.AuthFormMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signUpWithEmail: SignUpWithEmailUseCase,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)

    private val _formMode = MutableStateFlow(AuthFormMode.SIGN_IN)

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _errorRes = MutableStateFlow<Int?>(null)
    val errorRes: StateFlow<Int?> = _errorRes

    fun clearError() {
        _errorRes.value = null
    }

    fun submit(email: String, password: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorRes.value = null
            val result = when (_formMode.value) {
                AuthFormMode.SIGN_IN -> authRepository.signInWithEmail(email, password)
                AuthFormMode.SIGN_UP -> signUpWithEmail(email, password)
            }
            result.onFailure { _errorRes.value = it.toErrorRes() }
            _isSubmitting.value = false
        }
    }

    /** startTour = true — пользователь выбрал "Продолжить без аккаунта"
     * (гость, ни Firebase-аккаунта, ни данных — тур на демо-данных уместен).
     * startTour = false — пользователь вошёл в существующий аккаунт, у него
     * уже есть история использования, тур не нужен и был бы навязчив. */
    fun finishOnboarding(startTour: Boolean) {
        viewModelScope.launch { onboardingRepository.setCompleted(startTour) }
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