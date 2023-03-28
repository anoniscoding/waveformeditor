package com.anu.waveformeditor

sealed class  MainIntent {
    object OnImportTextFileEvent: MainIntent()
    data class OnSelectedRangeChangeEvent(val start: Float, val end: Float): MainIntent()
    data class OnExportSelectedRangeEvent(val selectedRange: List<Pair<Float, Float>>): MainIntent()
}

data class MainData(
    val waveformData: List<Pair<Float, Float>> = emptyList(),
    val isOpenDirectory: OneTimeEvent<Unit>? = null,
    val message: OneTimeEvent<String?>? = null,
    val normalizedSelectedRangeStart: Float = 0f, //Of represents start of waveform
    val normalizedSelectedRangeEnd: Float = 1f, //1f represents end of waveform
)