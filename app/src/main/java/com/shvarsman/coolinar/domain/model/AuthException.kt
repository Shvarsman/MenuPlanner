package com.shvarsman.coolinar.domain.model

/**
 * Ошибки аутентификации в domain-терминах, без утечки Firebase-типов
 * в presentation-слой. FirebaseAuthRepositoryImpl мапит исключения Firebase
 * SDK в эти классы.
 */
sealed class AuthException(message: String? = null) : Exception(message) {
    class InvalidEmail : AuthException()
    class WeakPassword : AuthException()
    class WrongCredentials : AuthException()
    class EmailAlreadyInUse : AuthException()
    class NetworkError : AuthException()
    class Unknown(message: String? = null) : AuthException(message)
}