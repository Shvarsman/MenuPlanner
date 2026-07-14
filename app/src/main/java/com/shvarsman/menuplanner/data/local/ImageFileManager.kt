package com.shvarsman.menuplanner.data.local

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

    private fun decodeSampledBitmap(sourceUri: Uri, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds, maxDimension)
        }
        return context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it, null, options)
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
    }
}
