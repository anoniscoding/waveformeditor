package com.anu.waveformeditor.domain

import com.anu.waveformeditor.data.FileIORepository

class ExportWaveformDataUseCase(private val fileIORepository: FileIORepository) {
    suspend operator fun invoke(waveformData: List<Pair<Float, Float>>): String {
        if (waveformData.isEmpty()){
            throw IllegalArgumentException("No selected region")
        }
        return fileIORepository.writeWaveFormDataToUri(waveformData)
    }
}