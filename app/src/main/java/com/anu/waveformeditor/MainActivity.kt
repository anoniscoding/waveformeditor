package com.anu.waveformeditor

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import java.io.File

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

        val useCase = ParseWaveFormDataUseCase(ContentResolverHelperImpl(contentResolver))
        viewModel = ViewModelProvider(this, MainViewModelFactory(useCase))[MainViewModel::class.java]

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

        it.selectedRange?.getContentIfNotHandled()?.let { range ->
            if (range.isEmpty()) {
                displayMessage(getString(R.string.label_no_selected_range))
                return@let
            }
            exportSelectedRangeAsTextFile(range)
        }

        it.errorMessage?.getContentIfNotHandled()?.let {
            displayMessage(it)
        }

        waveformView.updateNormalizedSelectedRange(
            it.normalizedSelectedRangeStart,
            it.normalizedSelectedRangeEnd
        )
    }

    private fun exportSelectedRangeAsTextFile(range: List<Pair<Float, Float>>) {
        val content = range.joinToString("\n") { "${it.first} ${it.second}" }
        val filename = "selected_range_${System.currentTimeMillis()}.txt"
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)
        file.writeText(content)

        displayMessage(getString(R.string.label_exported_wave_to_filename, filename))
    }

    private fun displayMessage(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }
}