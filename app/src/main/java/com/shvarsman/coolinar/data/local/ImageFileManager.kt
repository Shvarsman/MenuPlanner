package com.shvarsman.coolinar.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.filesDir, "recipe_images").apply { if (!exists()) mkdirs() }

    suspend fun persistImage(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(imagesDir, fileName)
        val bitmap = decodeSampledBitmap(sourceUri, MAX_DIMENSION_PX)
            ?: run {
                // Fallback: raw copy if decode fails (e.g. exotic format).
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                return@withContext destFile.toUri().toString()
            }
        try {
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
        } finally {
            bitmap.recycle()
        }
        destFile.toUri().toString()
    }

    /** Saves image bytes (used when restoring from a backup zip). */
    suspend fun persistImageBytes(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(imagesDir, fileName)
        val bitmap = decodeSampledBitmap(bytes, MAX_DIMENSION_PX)
        if (bitmap != null) {
            try {
                FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                }
            } finally {
                bitmap.recycle()
            }
        } else {
            destFile.writeBytes(bytes)
        }
        destFile.toUri().toString()
    }

    suspend fun deleteImage(uriString: String) = withContext(Dispatchers.IO) {
        runCatching {
            uriString.toUri().path?.let { path -> File(path).delete() }
        }
    }

    /**
     * Читает локальный файл по [uriString] и сжимает до примерно [maxBytes] —
     * используется для встраивания фото в документ Firestore как base64
     * (бесплатный план без Storage: лимит 1 МиБ на весь документ, поэтому
     * каждая картинка внутри него должна быть заметно меньше). Итеративно
     * снижает JPEG-качество, а если этого не хватает — уменьшает разрешение.
     * Возвращает null, если файл не читается (например, был удалён).
     */
    suspend fun readCompressedBytes(uriString: String, maxBytes: Int): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val uri = uriString.toUri()
                var dimension = MAX_DIMENSION_PX
                var bitmap = decodeSampledBitmap(uri, dimension) ?: return@withContext null
                try {
                    var quality = JPEG_QUALITY
                    var bytes = compressToBytes(bitmap, quality)
                    while (bytes.size > maxBytes && (quality > MIN_JPEG_QUALITY || dimension > MIN_DIMENSION_PX)) {
                        if (quality > MIN_JPEG_QUALITY) {
                            quality -= 15
                        } else {
                            dimension = (dimension * 0.75f).toInt().coerceAtLeast(MIN_DIMENSION_PX)
                            bitmap.recycle()
                            bitmap = decodeSampledBitmap(uri, dimension) ?: return@withContext bytes
                        }
                        bytes = compressToBytes(bitmap, quality)
                    }
                    bytes
                } finally {
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                // Файл мог быть удалён между тем, как мы решили его синхронизировать,
                // и моментом фактического чтения (замена обложки на другом потоке,
                // конкурентная синхронизация) — это не должно ронять приложение,
                // просто это фото не попадёт в этот цикл отправки.
                android.util.Log.w("ImageFileManager", "readCompressedBytes failed for $uriString", e)
                null
            }
        }

    private fun compressToBytes(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    private fun decodeSampledBitmap(sourceUri: Uri, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        try {
            context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            } ?: return null
        } catch (e: java.io.FileNotFoundException) {
            return null
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds, maxDimension)
        }
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: java.io.FileNotFoundException) {
            null
        }
    }

    private fun decodeSampledBitmap(bytes: ByteArray, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds, maxDimension)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxDimension: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        val longest = max(height, width)
        if (longest > maxDimension) {
            var half = longest / 2
            while (half / inSampleSize >= maxDimension) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        private const val MAX_DIMENSION_PX = 1600
        private const val JPEG_QUALITY = 85
        private const val MIN_JPEG_QUALITY = 40
        private const val MIN_DIMENSION_PX = 400
    }
}
