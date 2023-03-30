package com.anu.waveformeditor.domain

import com.anu.waveformeditor.data.FileIORepository

class ExportWaveformDataUseCase(private val fileIORepository: FileIORepository) {
    suspend operator fun invoke(waveformData: List<Pair<Float, Float>>): String {
        if (waveformData.isEmpty()) {
            throw IllegalArgumentException(NO_SELECTION_MESSAGE)
        }
        return fileIORepository.writeWaveFormDataToUri(waveformData)
    }

    companion object {
        const val NO_SELECTION_MESSAGE = "No selected region"
    }
}