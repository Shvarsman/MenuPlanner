package com.shvarsman.coolinar.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.shvarsman.coolinar.domain.model.AuthException
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.model.User
import com.shvarsman.coolinar.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser.toAuthState())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInAnonymously(): Result<Unit> = runCatchingAuth {
        firebaseAuth.signInAnonymously().await()
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        runCatchingAuth {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatchingAuth {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }

    override suspend fun linkAnonymousWithEmail(email: String, password: String): Result<Unit> =
        runCatchingAuth {
            val current = firebaseAuth.currentUser
                ?: throw AuthException.Unknown("Нет активной анонимной сессии")
            val credential = EmailAuthProvider.getCredential(email, password)
            current.linkWithCredential(credential).await()
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun runCatchingAuth(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: FirebaseAuthWeakPasswordException) {
        Result.failure(AuthException.WeakPassword())
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(AuthException.InvalidEmail())
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.failure(AuthException.EmailAlreadyInUse())
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.failure(AuthException.WrongCredentials())
    } catch (e: FirebaseNetworkException) {
        Result.failure(AuthException.NetworkError())
    } catch (e: AuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthException.Unknown(e.message))
    }
}

private fun FirebaseUser?.toAuthState(): AuthState =
    if (this == null) {
        AuthState.SignedOut
    } else {
        AuthState.SignedIn(
            User(
                uid = uid,
                email = email,
                displayName = displayName,
                isAnonymous = isAnonymous
            )
        )
    }