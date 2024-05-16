package com.dicoding.audioclassification

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import java.lang.IllegalStateException
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class AudioClassiferHelper(
    val threshold: Float = 0.1f,
    val maxResult: Int = 3,
    val modelName: String = "yamnet.tflite",
    val runningMode: RunningMode = RunningMode.AUDIO_STREAM,
    val overlap: Float = 0.5f,
    val context: Context,
    var classifierListener: ClassifierListener? = null
) {
    private var audioClassifier: AudioClassifier? = null
    private var recorder: AudioRecord? = null
    private var executor: ScheduledThreadPoolExecutor? = null

    init {
        initClassifier()
    }

    private fun streamAudioResultListener(resultListener: AudioClassifierResult) {
        classifierListener?.onResults(
            resultListener.classificationResults().first().classifications(),
            resultListener.timestampMs()
        )
    }


    private fun streamAudioErrorListener(e: RuntimeException?) {
        classifierListener?.onError(e?.message.toString())
    }

    private fun initClassifier() {
        try {
            val optionsBuilder = AudioClassifier.AudioClassifierOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResult)
                .setRunningMode(runningMode)

            if (runningMode == RunningMode.AUDIO_STREAM) {
                optionsBuilder
                    .setResultListener(this::streamAudioResultListener)
                    .setErrorListener(this::streamAudioErrorListener)
            }

            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(modelName)
            optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

            audioClassifier = AudioClassifier.createFromOptions(context, optionsBuilder.build())

            if (runningMode == RunningMode.AUDIO_STREAM) {
                recorder = audioClassifier?.createAudioRecord(
//                    AudioFormat.CHANNEL_IN_DEFAULT bernilai 1 yang artinya 1 channel.
                    AudioFormat.CHANNEL_IN_DEFAULT,
//                    menentukan frekuensi sampling audio yang akan direkam
                    SAMPLING_RATE_IN_HZ,
//                     menentukan ukuran buffer audio yang akan dipakai di penyimpanan Android
                    BUFFER_SIZE_IN_BYTES.toInt()
                )
            }
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.audio_classifier_failed))
            Log.e(TAG, "MP task failed to load with error: " + e.message)
        } catch (e: RuntimeException) {
            classifierListener?.onError(context.getString(R.string.audio_classifier_failed))
            Log.e(TAG, "MP task failed to load with error: " + e.message)
        }

    }

    fun startAudioClassification() {
        if (audioClassifier == null) {
            initClassifier()
        }

        if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return
        }

        recorder?.startRecording()
        executor = ScheduledThreadPoolExecutor(1)

        val classifyRunnable = Runnable{
            recorder?.let { classifyAudioAsync(it) }
        }

        val lengthMilliSeconds = ((REQUIRE_INPUT_BUFFER_SIZE * 1.0f) / SAMPLING_RATE_IN_HZ) * 1000
        val interval = (lengthMilliSeconds * (1 - overlap)).toLong()

        executor?.scheduleAtFixedRate(

//            Runnable: tugas yang dijalankan setelah interval tertentu.
            classifyRunnable,
//            initialDelay: waktu awal untuk memulai tugas. Dalam hal ini 0, artinya tugas akan dimulai segera.
            0,
//            interval: interval antara dua tugas.
            interval,
//            TimeUnit: satuan untuk interval yang didefinisikan. Dalam hal ini milidetik.
            TimeUnit.MILLISECONDS
        )
    }

    private fun classifyAudioAsync(audioRecord: AudioRecord) {
        val audioData = AudioData.create(
            AudioData.AudioDataFormat.create(recorder?.format), SAMPLING_RATE_IN_HZ
        )
        audioData.load(audioRecord)

        val inferenceTime = SystemClock.uptimeMillis()
        audioClassifier?.classifyAsync(audioData, inferenceTime)
    }

    fun stopAudioClassification() {
        executor?.shutdownNow()
        audioClassifier?.close()
        audioClassifier = null
        recorder?.stop()
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            results: List<Classifications>,
            inferenceTime: Long
        )
    }

    companion object {
        private const val TAG = "AudioClassifierHelper"

        private const val SAMPLING_RATE_IN_HZ = 16000
        private const val EXPECTED_INPUT_LENGTH = 0.975F
        private const val REQUIRE_INPUT_BUFFER_SIZE = SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH
        private const val BUFFER_SIZE_FACTOR: Int = 2
        private const val BUFFER_SIZE_IN_BYTES =
            REQUIRE_INPUT_BUFFER_SIZE * Float.SIZE_BYTES * BUFFER_SIZE_FACTOR

//        Float.SIZE_BYTES: konstanta yang digunakan dalam pemrograman untuk menentukan jumlah byte
    //        yang diperlukan untuk merepresentasikan sebuah nilai float dalam bentuk biner
//        BUFFER_SIZE_FACTOR: digunakan untuk memperhitungkan potensi kesalahan dalam perekaman
    }
}