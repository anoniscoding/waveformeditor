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
                        ?: throw IllegalArgumentException(INVALID_MESSAGE)
                }
                if (verticalRange.size != MAX_WAVE_PAIR_LENGTH) {
                    throw IllegalArgumentException(INVALID_MESSAGE)
                }
                Pair(verticalRange.first(), verticalRange.last())
            }

        if (result.isEmpty()) {
            throw IllegalArgumentException(EMPTY_MESSAGE)
        }

        return result
    }

    companion object {
        const val MAX_WAVE_PAIR_LENGTH = 2
        const val INVALID_MESSAGE = "Invalid waveform data format"
        const val EMPTY_MESSAGE = "Waveform data is empty"
    }
}