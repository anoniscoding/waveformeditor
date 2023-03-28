package com.anu.waveformeditor

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentResolverHelperImpl(private val contentResolver: ContentResolver) : ContentResolverHelper {
    override suspend fun readTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
    }
}
