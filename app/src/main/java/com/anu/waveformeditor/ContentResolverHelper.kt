package com.anu.waveformeditor

import android.net.Uri

interface ContentResolverHelper {
    suspend fun readTextFromUri(uri: Uri): String
    suspend fun writeWaveFormDataToUri(waveformData: List<Pair<Float, Float>>): String
}
