package com.anu.waveformeditor.presentation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anu.waveformeditor.OneTimeEvent
import com.anu.waveformeditor.domain.ExportWaveformDataUseCase
import com.anu.waveformeditor.domain.ImportWaveFormDataUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(
    private val importWaveformDataUseCase: ImportWaveFormDataUseCase,
    private val exportWaveformDataUseCase: ExportWaveformDataUseCase
): ViewModel() {

    private val _viewData = MutableLiveData(MainData())
    val viewData: LiveData<MainData> = _viewData

    fun setIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.OnExportSelectedRangeEvent -> onExportSelectedRange(intent.selectedRange)
            is MainIntent.OnSelectedRangeChangeEvent -> updateSelectedRange(intent.start, intent.end)
            MainIntent.OnImportTextFileEvent -> onImportTextFile()
        }
    }

    private fun setWaveFormData(waveData: List<Pair<Float, Float>>) {
        _viewData.value = _viewData.value?.copy(
            waveformData = waveData,
            normalizedSelectedRangeStart = 0f,
            normalizedSelectedRangeEnd = 1f
        )
    }

    private fun onExportSelectedRange(selectedRange: List<Pair<Float, Float>>) {
        launchRequest {
            val fileName = exportWaveformDataUseCase(selectedRange)
            _viewData.value = _viewData.value?.copy(
                message = OneTimeEvent("Exported $fileName successfully")
            )
        }
    }

    private fun onImportTextFile() {
        _viewData.value = _viewData.value?.copy(isOpenDirectory = OneTimeEvent(Unit))
    }

    fun readAndSetWaveformData(uri: Uri) {
        launchRequest {
            val waveData = importWaveformDataUseCase(uri)
            setWaveFormData(waveData)
        }
    }

    private fun launchRequest(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                block()
            } catch (exception: Exception) {
                _viewData.value = _viewData.value?.copy(
                    message = OneTimeEvent(exception.message)
                )
            }
        }
    }

    private fun updateSelectedRange(start: Float, end: Float) {
        _viewData.value = _viewData.value?.copy(
            normalizedSelectedRangeStart = start,
            normalizedSelectedRangeEnd = end
        )
    }
}