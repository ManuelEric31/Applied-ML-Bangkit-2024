package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import java.io.IOException


class ImageClassifierHelper(
    private val threshold: Float = 0.1f,
    private val maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    private val context: Context,
    private val classifierListener: ClassifierListener?
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionsBuilder = ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        // TODO: mengklasifikasikan imageUri dari gambar statis.
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(CastOp(DataType.UINT8))
                .build()

            val tensorImage = TensorImage(DataType.UINT8)
            tensorImage.load(bitmap)
            val processedImageClassify = imageProcessor.process(tensorImage)

            val inferenceTime: Long
            val startTime = SystemClock.uptimeMillis()
            val results = imageClassifier?.classify(processedImageClassify)
            val endTime = SystemClock.uptimeMillis()
            inferenceTime = endTime - startTime

            classifierListener?.onResult(results, inferenceTime)
        } catch (e: IOException) {
            classifierListener?.onError("Error Loading Image Classifier: ${e.message}")
            Log.e(TAG, "Error Loading Image Classifier: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

}

interface ClassifierListener {
    fun onError(error: String)
    fun onResult(
        results: List<Classifications>?,
        inferenceTime : Long
    )
}

