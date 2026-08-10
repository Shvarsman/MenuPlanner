package com.shvarsman.coolinar.data.remote.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

/**
 * Обёртка над Firebase Storage: загружает локальные файлы рецептов и
 * возвращает постоянный download URL. putFile() стримит файл с диска
 * напрямую, без декодирования в Bitmap в память — не подвержено OOM
 * на больших фото, в отличие от прежнего base64-подхода.
 */
@Singleton
class RemoteImageUploader @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun upload(uid: String, recipeId: String, localFileUri: String): String {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("users/$uid/recipe_images/$recipeId/$fileName")
        ref.putFile(localFileUri.toUri()).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun delete(remoteUrl: String) {
        runCatching { storage.getReferenceFromUrl(remoteUrl).delete().await() }
    }
}