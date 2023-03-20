package com.anu.waveformeditor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val _viewData = MutableLiveData(MainData())
    val viewData: LiveData<MainData> = _viewData

    fun setIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.OnExportSelectedRangeEvent -> onExportSelectedRange(intent.selectedRange)
            MainIntent.OnImportTextFileEvent -> onImportTextFile()
        }
    }

    fun setWaveFormData(waveData: List<Pair<Float, Float>>) {
        _viewData.value = _viewData.value?.copy(waveformData = waveData)
    }

    private fun onExportSelectedRange(selectedRange: List<Pair<Float, Float>>) {
        _viewData.value = _viewData.value?.copy(selectedRange = OneTimeEvent(selectedRange))
    }

    private fun onImportTextFile() {
        _viewData.value = _viewData.value?.copy(isOpenDirectory = OneTimeEvent(Unit))
    }
}