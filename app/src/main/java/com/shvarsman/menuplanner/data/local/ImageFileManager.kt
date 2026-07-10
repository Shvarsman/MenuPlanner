package com.shvarsman.menuplanner.data.local

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.filesDir, "recipe_images").apply { if (!exists()) mkdirs() }

    suspend fun persistImage(sourceUri: Uri): String {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(imagesDir, fileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        return destFile.toUri().toString()
    }

    /** Сохраняет изображение из массива байтов (используется при восстановлении
     * из резервной копии, где фото извлекаются из zip-архива). */
    suspend fun persistImageBytes(bytes: ByteArray): String {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(imagesDir, fileName)
        destFile.writeBytes(bytes)
        return destFile.toUri().toString()
    }

    fun deleteImage(uriString: String) {
        runCatching {
            uriString.toUri().path?.let { path -> File(path).delete() }
        }
    }
}