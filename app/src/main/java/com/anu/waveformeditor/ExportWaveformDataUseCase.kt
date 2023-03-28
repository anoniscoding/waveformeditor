package com.anu.waveformeditor

class ExportWaveformDataUseCase(private val contentResolverHelper: ContentResolverHelper) {
    suspend operator fun invoke(waveformData: List<Pair<Float, Float>>): String {
        return contentResolverHelper.writeWaveFormDataToUri(waveformData)
    }
}