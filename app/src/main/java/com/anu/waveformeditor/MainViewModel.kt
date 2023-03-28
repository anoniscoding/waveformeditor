package com.anu.waveformeditor

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(
    private val parseWaveformDataUseCase: ParseWaveFormDataUseCase
): ViewModel() {

    private val _viewData = MutableLiveData(MainData())
    val viewData: LiveData<MainData> = _viewData

    fun setIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.OnExportSelectedRangeEvent -> onExportSelectedRange(intent.selectedRange)
            MainIntent.OnImportTextFileEvent -> onImportTextFile()
        }
    }

    private fun setWaveFormData(waveData: List<Pair<Float, Float>>) {
        _viewData.value = _viewData.value?.copy(waveformData = waveData)
    }

    private fun onExportSelectedRange(selectedRange: List<Pair<Float, Float>>) {
        _viewData.value = _viewData.value?.copy(selectedRange = OneTimeEvent(selectedRange))
    }

    private fun onImportTextFile() {
        _viewData.value = _viewData.value?.copy(isOpenDirectory = OneTimeEvent(Unit))
    }

    fun readAndSetWaveformData(uri: Uri) {
        viewModelScope.launch {
            try {
                val waveData = parseWaveformDataUseCase(uri)
                setWaveFormData(waveData)
            } catch (exception: Exception) {
                _viewData.value = _viewData.value?.copy(errorMessage = OneTimeEvent(exception.message))
            }
        }
    }
}