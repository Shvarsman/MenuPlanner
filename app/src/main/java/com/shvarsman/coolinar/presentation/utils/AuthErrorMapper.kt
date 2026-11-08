package com.shvarsman.coolinar.presentation.utils

import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthException

fun Throwable.toAuthErrorRes(): Int = when (this) {
    is AuthException.InvalidEmail -> R.string.profile_error_invalid_email
    is AuthException.WeakPassword -> R.string.profile_error_weak_password
    is AuthException.WrongCredentials -> R.string.profile_error_wrong_credentials
    is AuthException.EmailAlreadyInUse -> R.string.profile_error_email_in_use
    is AuthException.NetworkError -> R.string.profile_error_network
    else -> R.string.profile_error_generic
}
