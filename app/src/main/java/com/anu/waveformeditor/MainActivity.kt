package com.anu.waveformeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var waveformView: WaveformView
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var viewModel: MainViewModel

    private val importTextFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    val fileUri = result?.data?.data
                    fileUri?.let { uri ->
                        viewModel.setWaveFormData(parseWaveformData(uri))
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, MainViewModelFactory())[MainViewModel::class.java]

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

    private suspend fun parseWaveformData(uri: Uri): List<Pair<Float, Float>> {
        return withContext(Dispatchers.IO) {
            val waveformData = mutableListOf<Pair<Float, Float>>()
            contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    val verticalRange = line.trim().split("\\s+".toRegex())
                    if (verticalRange.size == MAX_WAVE_PAIR_LENGTH) {
                        val min = verticalRange.first().toFloatOrNull() ?: 0f
                        val max = verticalRange.last().toFloatOrNull() ?: 0f
                        waveformData.add(min to max)
                    }
                }
            }
            waveformData
        }
    }

    private fun displayMessage(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val MAX_WAVE_PAIR_LENGTH = 2
    }
}