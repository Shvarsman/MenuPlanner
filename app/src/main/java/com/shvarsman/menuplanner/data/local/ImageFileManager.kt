package com.shvarsman.menuplanner.data.local

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Копирует изображения, выбранные через системный пикер, во внутреннее хранилище
 * приложения. Это необходимо, так как content:// URI из GetContent() даёт лишь
 * временное разрешение на чтение — оно исчезает после перезапуска процесса,
 * из-за чего фото "пропадают" при повторном открытии приложения.
 */
class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.filesDir, "recipe_images").apply { if (!exists()) mkdirs() }

    /** Копирует изображение по [sourceUri] во внутреннее хранилище,
     * возвращает постоянный file:// URI в виде строки. */
    suspend fun persistImage(sourceUri: Uri): String {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(imagesDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        return destFile.toUri().toString()
    }

    /** Удаляет файл изображения по сохранённому URI (best-effort). */
    fun deleteImage(uriString: String) {
        runCatching {
            uriString.toUri().path?.let { path -> File(path).delete() }
        }
    }
}