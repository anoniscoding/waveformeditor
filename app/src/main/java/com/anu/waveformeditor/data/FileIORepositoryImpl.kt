package com.anu.waveformeditor.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class FileIORepositoryImpl(
    private val context: Context,
) : FileIORepository {
    override suspend fun readTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
    }

    override suspend fun writeWaveFormDataToUri(waveformData: List<Pair<Float, Float>>): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeDataToUriForAPIAboveQ(waveformData)
        } else {
            writeDataToUriForAPIBelowQ(waveformData)
        }
    }

    private suspend fun writeDataToUriForAPIBelowQ(waveformData: List<Pair<Float, Float>>): String {
        return withContext(Dispatchers.IO) {
            val (filename, uri) = getFilenameWithUri()
            writeToFile(uri, waveformData)
            filename
        }
    }

    private fun getFilenameWithUri(): Pair<String, Uri> {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filename = "selected_waveform_${System.currentTimeMillis()}.txt"
        val file = File(downloadsFolder, filename)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Pair(filename, uri)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun writeDataToUriForAPIAboveQ(waveformData: List<Pair<Float, Float>>): String {
        val fileName = "selected_waveform_${System.currentTimeMillis()}.txt"

        return withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri =
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Failed to create file")
            writeToFile(uri, waveformData)
            fileName
        }
    }

    private fun writeToFile(
        uri: Uri,
        waveformData: List<Pair<Float, Float>>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                waveformData.forEach { (min, max) ->
                    writer.write("$min $max\n")
                }
            }
        }
    }

}
