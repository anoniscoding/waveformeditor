package com.anu.waveformeditor

import android.net.Uri

class ImportWaveFormDataUseCase (private val fileIORepository: FileIORepository) {
    suspend operator fun invoke(uri: Uri): List<Pair<Float, Float>> {
        val data = fileIORepository.readTextFromUri(uri)
        return parseWaveformData(data)
    }

    private fun parseWaveformData(data: String): List<Pair<Float, Float>> {
        val result = data.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val verticalRange = line.split("\\s+".toRegex()).map { it.trim().toFloatOrNull() ?: 0f }
                if (verticalRange.size != MAX_WAVE_PAIR_LENGTH) {
                    throw IllegalArgumentException("Invalid waveform data format")
                }
                Pair(verticalRange.first(), verticalRange.last())
            }

        if (result.isEmpty()) {
            throw IllegalArgumentException("Waveform data is empty")
        }

        return result
    }

    companion object {
        const val MAX_WAVE_PAIR_LENGTH = 2
    }
}