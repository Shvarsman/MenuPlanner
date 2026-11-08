package com.shvarsman.coolinar.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.shvarsman.coolinar.data.local.ImageFileManager
import com.shvarsman.coolinar.data.remote.storage.RemoteImageUploader
import com.shvarsman.coolinar.domain.model.AuthException
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.domain.model.User
import com.shvarsman.coolinar.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val imageFileManager: ImageFileManager,
    private val remoteImageUploader: RemoteImageUploader
) : AuthRepository {

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    private val _authState = MutableStateFlow(firebaseAuth.currentUser.toAuthState())
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _authState.value = auth.currentUser.toAuthState()
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        runCatchingAuth {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            return@runCatchingAuth
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatchingAuth {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return@runCatchingAuth
        }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        runCatchingAuth {
            val user = firebaseAuth.currentUser
                ?: throw AuthException.Unknown("Нет активной сессии")
            val email = user.email
                ?: throw AuthException.Unknown("У аккаунта нет email")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
        }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> =
        runCatchingAuth {
            val user = firebaseAuth.currentUser
                ?: throw AuthException.Unknown("Нет активной сессии")
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            ).await()
            refreshCurrentUser()
        }

    override suspend fun updatePhotoUrl(photoUrl: String?): Result<Unit> =
        runCatchingAuth {
            val user = firebaseAuth.currentUser
                ?: throw AuthException.Unknown("Нет активной сессии")
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setPhotoUri(photoUrl?.toUri())
                    .build()
            ).await()
            refreshCurrentUser()
        }

    override suspend fun uploadAvatar(sourceUri: Uri): Result<String> =
        runCatchingAuth {
            val uid = currentUserId ?: throw AuthException.Unknown("Нет активной сессии")
            val localUri = imageFileManager.persistImage(sourceUri)
            val remoteUrl = try {
                remoteImageUploader.uploadAvatar(uid, localUri)
            } finally {
                imageFileManager.deleteImage(localUri)
            }
            updatePhotoUrl(remoteUrl).getOrThrow()
            remoteUrl
        }

    override suspend fun removeAvatar(): Result<Unit> =
        runCatchingAuth {
            val currentUrl = firebaseAuth.currentUser?.photoUrl?.toString()
            updatePhotoUrl(null).getOrThrow()
            if (!currentUrl.isNullOrBlank()) {
                runCatching { remoteImageUploader.delete(currentUrl) }
            }
        }

    private suspend fun refreshCurrentUser() {
        firebaseAuth.currentUser?.reload()?.await()
        _authState.value = firebaseAuth.currentUser.toAuthState()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun <T> runCatchingAuth(block: suspend () -> T): Result<T> = try {
        Result.success(block())
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
                photoUrl = photoUrl?.toString()
            )
        )
    }