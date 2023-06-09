package com.anu.waveformeditor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.anu.waveformeditor.data.FileIORepositoryImpl
import com.anu.waveformeditor.domain.ExportWaveformDataUseCase
import com.anu.waveformeditor.domain.ImportWaveFormDataUseCase
import com.anu.waveformeditor.presentation.MainData
import com.anu.waveformeditor.presentation.MainIntent
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

    private val writePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.setIntent(MainIntent.OnExportSelectedRangeEvent(waveformView.getAllPairsInSelectedRange()))
            } else {
                toast(getString(R.string.label_write_permission_is_needed))
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val importUseCase = ImportWaveFormDataUseCase(FileIORepositoryImpl( this))
        val exportUseCase = ExportWaveformDataUseCase(FileIORepositoryImpl( this))
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(importUseCase, exportUseCase)
        )[MainViewModel::class.java]

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
            onExportButtonClicked()
        }

        importButton.setOnClickListener {
            viewModel.setIntent(MainIntent.OnImportTextFileEvent)
        }

        waveformView.onSelectedRangeChanged = { startX, endX ->
            viewModel.setIntent(MainIntent.OnSelectedRangeChangeEvent(startX, endX))
        }
    }

    private fun onExportButtonClicked() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                viewModel.setIntent(MainIntent.OnExportSelectedRangeEvent(waveformView.getAllPairsInSelectedRange()))
            }
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.setIntent(MainIntent.OnExportSelectedRangeEvent(waveformView.getAllPairsInSelectedRange()))
            }
            else -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    toast(getString(R.string.label_write_permission_rationale))
                } else {
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun onViewDataReceived(viewData: MainData) {
        waveformView.setWaveformData(viewData.waveformData)

        viewData.isOpenDirectory?.getContentIfNotHandled()?.let {
            importTextFileLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            })
        }

        viewData.message?.getContentIfNotHandled()?.let {
            toast(it)
        }

        waveformView.updateSelectedRange(
            viewData.normalizedSelectedRangeStart,
            viewData.normalizedSelectedRangeEnd
        )
    }
}