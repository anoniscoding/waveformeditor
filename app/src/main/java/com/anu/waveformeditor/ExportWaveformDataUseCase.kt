package com.anu.waveformeditor

class ExportWaveformDataUseCase(private val fileIORepository: FileIORepository) {
    suspend operator fun invoke(waveformData: List<Pair<Float, Float>>): String {
        return fileIORepository.writeWaveFormDataToUri(waveformData)
    }
}