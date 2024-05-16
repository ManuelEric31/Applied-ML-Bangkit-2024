package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.factory.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var resultViewModel: ResultViewModel
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultViewModel = obtainViewModel(this@ResultActivity)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val classificationLabel = intent.getStringExtra(EXTRA_LABEL_RESULT)
        val classificatioConfident = intent.getStringExtra(EXTRA_CONF_SCORE)

        showLoading(true)
        if (imageUri != null) {
            showLoading(false)
            val fabAdd: FloatingActionButton = binding.fabAdd
            imageUri.let {
                val resultText = getString(R.string.label_result, classificationLabel)
                val confidenceText = getString(R.string.label_confidence, classificatioConfident)
                Log.d(TAG, "showImage: $it")

                binding.resultImage.setImageURI(it)
                binding.resultText.text = getString(R.string.result_text, resultText, confidenceText)
                resultViewModel.getHistoryByUri(imageUri.toString())
                    .observe(this@ResultActivity) {result ->
                        if (result != null) {
                            binding.fabAdd.setImageResource(R.drawable.baseline_bookmark_added_24)
                            isFavorite = false
                        } else {
                            binding.fabAdd.setImageResource(R.drawable.baseline_bookmark_border_24)
                            isFavorite = true
                        }
                    }
            }

            fabAdd.setOnClickListener{
                imageUri.let {
                    val resultUser = HistoryUser(
                        category = classificationLabel.toString(),
                        confident = classificatioConfident.toString(),
                        uriImage = imageUri.toString()
                    )
                    if (isFavorite) {
                        resultViewModel.insert(resultUser)
                        Toast.makeText(
                            this,
                            getString(R.string.toast_add_favorite),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        resultViewModel.delete(resultUser)
                        Toast.makeText(this,
                            getString(R.string.toast_delete_favorite), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        resultViewModel.isLoading.observe(this){
            showLoading(it)
        }
    }

    private fun obtainViewModel(activity: AppCompatActivity): ResultViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[ResultViewModel::class.java]
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CONF_SCORE = "extra_conf_score"
        const val EXTRA_LABEL_RESULT = "extra_label_result"
        const val TAG = "ImageUri"
    }

}
