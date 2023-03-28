package com.anu.waveformeditor

import android.net.Uri

interface FileIORepository {
    suspend fun readTextFromUri(uri: Uri): String
    suspend fun writeWaveFormDataToUri(waveformData: List<Pair<Float, Float>>): String
}
