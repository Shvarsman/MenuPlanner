package com.shvarsman.coolinar.domain.model

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}