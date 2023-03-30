package com.anu.waveformeditor.domain

import android.net.Uri
import com.anu.waveformeditor.data.FileIORepository

class ImportWaveFormDataUseCase (private val fileIORepository: FileIORepository) {
    suspend operator fun invoke(uri: Uri): List<Pair<Float, Float>> {
        val data = fileIORepository.readTextFromUri(uri)
        return parseWaveformData(data)
    }

    private fun parseWaveformData(data: String): List<Pair<Float, Float>> {
        val result = data.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val verticalRange = line.split("\\s+".toRegex()).map {
                    it.trim().toFloatOrNull()
                        ?: throw IllegalArgumentException(INVALID_FORMAT)
                }
                if (verticalRange.size != MAX_WAVE_PAIR_LENGTH) {
                    throw IllegalArgumentException(INVALID_FORMAT)
                }
                Pair(verticalRange.first(), verticalRange.last())
            }

        if (result.isEmpty()) {
            throw IllegalArgumentException(EMPTY_FORMAT)
        }

        return result
    }

    companion object {
        const val MAX_WAVE_PAIR_LENGTH = 2
        const val INVALID_FORMAT = "Invalid waveform data format"
        const val EMPTY_FORMAT = "Waveform data is empty"
    }
}