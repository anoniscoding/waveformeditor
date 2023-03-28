package com.anu.waveformeditor.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

class FileIORepositoryImpl(
    private val contentResolver: ContentResolver,
    private val context: Context,
) : FileIORepository {
    override suspend fun readTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
    }

    override suspend fun writeWaveFormDataToUri(waveformData: List<Pair<Float, Float>>): String {
        return withContext(Dispatchers.IO) {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val filename = "selected_range_${System.currentTimeMillis()}.txt"
            val file = File(downloadsFolder, filename)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    waveformData.forEach { (min, max) ->
                        writer.write("$min $max\n")
                    }
                }
            }
            filename
        }
    }
}
