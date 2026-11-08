package com.shvarsman.coolinar.domain.repository

import android.net.Uri

interface ImageStorageRepository {
    suspend fun persistImage(sourceUri: Uri): String
    suspend fun deleteImage(uriString: String): Boolean
}
