package com.anu.waveformeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(
    private val parseWaveformDataUseCase: ParseWaveFormDataUseCase,
    private val exportWaveformDataUseCase: ExportWaveformDataUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(parseWaveformDataUseCase, exportWaveformDataUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}