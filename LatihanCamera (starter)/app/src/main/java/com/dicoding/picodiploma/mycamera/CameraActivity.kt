package com.dicoding.picodiploma.mycamera

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.mycamera.databinding.ActivityCameraBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var barcodeScanner: BarcodeScanner
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var firstCall = true
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    private fun startCamera() {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResult(results: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                println(it)
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.label} " + NumberFormat.getPercentInstance()
                                            .format(it.score).trim()
                                    }
                                binding.tvResult.text = displayResult
                                binding.tvInferenceTime.text = "$inferenceTime ms"
                            } else {
                                binding.tvResult.text = ""
                                binding.tvInferenceTime.text = ""
                            }
                        }
                    }
                }
            }
        )
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) {image ->
                imageClassifierHelper.classifyImage(image)
            }

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    "Gagal memunculkan kamera.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))


//// --Bagian Latihan Scan Barcode menggunakan ML Kit--
//        val options = BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
//            .build()
//        barcodeScanner = BarcodeScanning.getClient(options)
//
//        val analyzer = MlKitAnalyzer(
////            Parameter pertama kita menentukan detector yang digunakan, dalam hal ini yaitu
////            barcodeScanner.
////            Karena dalam bentuk list, Anda juga dapat menambahkan jenis ML Kit API yang lain.
//            listOf(barcodeScanner),
////            Dengan memasukkan konstanta tersebut, sistem akan mengambil koordinat
////            dari hasil pemindaian gambar dan mengonversinya ke koordinat PreviewView secara otomatis.
//            COORDINATE_SYSTEM_VIEW_REFERENCED,
////            Parameter ketiga adalah Executor. Dengan menggunakan getMainExecutor,
////            kita dapat menampilkan hasil analisis di UI secara langsung.
//            ContextCompat.getMainExecutor(this)
//        ) { result: MlKitAnalyzer.Result? ->
////            Show result
//            showResult(result)
//        }
//
//        val cameraController = LifecycleCameraController(baseContext)
//        cameraController.setImageAnalysisAnalyzer(
//            ContextCompat.getMainExecutor(this),
//            analyzer
//        )
//        cameraController.bindToLifecycle(this)
//        binding.viewFinder.controller = cameraController


    }

    //    MLKitAnalyzer merupakan wrapper yang berguna untuk menghubungkan ML Kit dengan CameraController pada CameraX.
    private fun showResult(result: MlKitAnalyzer.Result?) {
        if (firstCall) {
            val barcodeResults = result?.getValue(barcodeScanner)
            if ((barcodeResults != null) &&
                (barcodeResults.size != 0) &&
                (barcodeResults.first() != null)
            ) {
                firstCall = false
                val barcode = barcodeResults[0]
                val alertDialog = AlertDialog.Builder(this)
                    .setMessage(barcode.rawValue)
                    .setPositiveButton(
                        "Buka"
                    ) { _, _ ->
                        firstCall = true
                        when (barcode.valueType) {
                            Barcode.TYPE_URL -> {
                                val openBrowserIntent = Intent(Intent.ACTION_VIEW)
                                openBrowserIntent.data = Uri.parse(barcode.url?.url)
                                startActivity(openBrowserIntent)
                            }

                            else -> {
                                Toast.makeText(this, "Unsupported Data Type", Toast.LENGTH_SHORT)
                                    .show()
                                startCamera()
                            }
                        }
                    }
                    .setNegativeButton("Scan Lagi") { _, _ ->
                        firstCall = true
                    }
                    .setCancelable(false)
                    .create()
                alertDialog.show()
            }
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val CAMERAX_RESULT = 200
    }
}