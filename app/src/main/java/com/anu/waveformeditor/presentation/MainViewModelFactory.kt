package com.anu.waveformeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anu.waveformeditor.domain.ExportWaveformDataUseCase
import com.anu.waveformeditor.domain.ImportWaveFormDataUseCase

class MainViewModelFactory(
    private val parseWaveformDataUseCase: ImportWaveFormDataUseCase,
    private val exportWaveformDataUseCase: ExportWaveformDataUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(parseWaveformDataUseCase, exportWaveformDataUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}