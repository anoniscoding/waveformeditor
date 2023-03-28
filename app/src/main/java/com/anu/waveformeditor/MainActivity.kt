package com.anu.waveformeditor

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.anu.waveformeditor.data.FileIORepositoryImpl
import com.anu.waveformeditor.domain.ExportWaveformDataUseCase
import com.anu.waveformeditor.domain.ImportWaveFormDataUseCase
import com.anu.waveformeditor.presentation.MainViewModel
import com.anu.waveformeditor.presentation.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var waveformView: WaveformView
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var viewModel: MainViewModel

    private val importTextFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result?.data?.data
                fileUri?.let { uri ->
                    viewModel.readAndSetWaveformData(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parseUseCase = ImportWaveFormDataUseCase(FileIORepositoryImpl(contentResolver, this))
        val exportUseCase = ExportWaveformDataUseCase(FileIORepositoryImpl(contentResolver, this))
        viewModel = ViewModelProvider(this, MainViewModelFactory(parseUseCase, exportUseCase))[MainViewModel::class.java]

        waveformView = findViewById(R.id.waveform_view)
        exportButton = findViewById(R.id.export_button)
        importButton = findViewById(R.id.import_button)

        viewModel.viewData.observe(this) {
            onViewDataReceived(it)
        }
    }

    override fun onResume() {
        super.onResume()

        exportButton.setOnClickListener {
            viewModel.setIntent(MainIntent.OnExportSelectedRangeEvent(waveformView.getAllPairsInSelectedRange()))
        }

        importButton.setOnClickListener {
            viewModel.setIntent(MainIntent.OnImportTextFileEvent)
        }

        waveformView.onSelectedRangeChanged = { startX, endX ->
            viewModel.setIntent(MainIntent.OnSelectedRangeChangeEvent(startX, endX))
        }
    }

    private fun onViewDataReceived(it: MainData) {
        waveformView.setWaveformData(it.waveformData)

        it.isOpenDirectory?.getContentIfNotHandled()?.let {
            importTextFileLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            })
        }

        it.message?.getContentIfNotHandled()?.let {
            toast(it)
        }

        waveformView.updateNormalizedSelectedRange(
            it.normalizedSelectedRangeStart,
            it.normalizedSelectedRangeEnd
        )
    }
}