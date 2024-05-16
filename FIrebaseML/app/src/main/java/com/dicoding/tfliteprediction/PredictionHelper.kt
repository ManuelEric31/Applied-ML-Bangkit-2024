package com.dicoding.tfliteprediction

import android.content.Context
import android.util.Log
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.GpuDelegateFactory
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.Exception

class PredictionHelper(
    private val modelName: String = "rice_stock.tflite",
    val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onDownloadSuccess: () -> Unit
) {

    private var interpreter: InterpreterApi? = null

    init {
        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLite.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            downloadModel()
        }.addOnFailureListener {
            onError(context.getString(R.string.tflite_is_not_initialized_yet))
        }
    }

    @Synchronized
    private fun downloadModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel("Rice-Stock", DownloadType.LOCAL_MODEL, conditions)
            .addOnSuccessListener { model:CustomModel ->
                try {
//                    Download is success and initialize a prediction helper
                    onDownloadSuccess()
                    initializeInterpreter(model)
                } catch (e: IOException) {
                    onError(e.message.toString())
                }
            }
            .addOnFailureListener{e: Exception? ->
                onError(context.getString(R.string.firebaseml_model_download_failed))
            }
    }

    private fun initializeInterpreter(model: Any) {
        interpreter?.close()
        try {
            val options = InterpreterApi.Options()
                .setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
                .addDelegateFactory(GpuDelegateFactory())
            if (model is ByteBuffer) {
                interpreter = InterpreterApi.create(model, options)
            } else if (model is CustomModel) {
                model.file?.let {
                    interpreter = InterpreterApi.create(it, options)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
            Log.e(TAG, e.message.toString())
        }
    }

    fun close() {
        interpreter?.close()
    }

    fun predict(inputString: String) {
        if (interpreter == null) {
            return
        }

//        Menggunakan FloatArray karena data yang dianalisis pada model berupa angka desimal.
        val inputArray = FloatArray(1)
        inputArray[0] = inputString.toFloat()
        val outputArray = Array(1) { FloatArray(1) }
        try {
            interpreter?.run(inputArray, outputArray)
            onResult(outputArray[0][0].toString())
        } catch (e: Exception) {
            onError(context.getString(R.string.no_tflite_interpreter_loaded))
            Log.e(TAG, e.message.toString())
        }
    }

    companion object {
        private const val TAG = "PredictionHelper"
    }
}