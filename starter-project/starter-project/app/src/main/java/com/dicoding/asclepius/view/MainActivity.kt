package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ClassifierListener
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.view.news.NewsActivity
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private var croppedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mainViewModel.imageUri.observe(this) { uri ->
            uri?.let {
                croppedImageUri = uri
                showImage()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            croppedImageUri?.let {
                analyzeImage()
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
        binding.articleButton.setOnClickListener { startArticle() }
        binding.historyButton.setOnClickListener { startHistory() }
    }

    private fun startHistory() {
        val intent = Intent(this@MainActivity, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun startArticle() {
        val intent = Intent(this@MainActivity, NewsActivity::class.java)
        startActivity(intent)
    }

    private var cropImage = registerForActivityResult(
        CropImageContract()
    ) { result: CropImageView.CropResult ->
        if (result.isSuccessful) {
            val crop =
                BitmapFactory.decodeFile(result.getUriFilePath(applicationContext, true))
            binding.previewImageView.setImageBitmap(crop)
            croppedImageUri = result.uriContent
            mainViewModel.setImageUri(croppedImageUri)
        }
    }

    private fun cropImage(uri: Uri) {
        cropImage.launch(
            CropImageContractOptions(
                uri = uri, cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON
                )
            )
        )
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            croppedImageUri = uri

            mainViewModel.setImageUri(uri)

            cropImage(uri)

            showImage()

        } else {
            Log.d(TAG_PHOTOPICKER, "No media selected!, Please select media")
        }
    }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        val imageUriToShow = croppedImageUri
        imageUriToShow?.let {
            Log.d(TAG_IMAGE, "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ClassifierListener {
                override fun onResult(results: List<Classifications>?, inferenceTime: Long) {
                    results?.let {
                        val classificationResult = it[0].categories[0].label
                        val classificationConfident = (it[0].categories[0].score * 100).toString()
                        moveToResult(classificationResult, classificationConfident)
                    } ?: showToast(getString(R.string.image_classifier_failed))
                }

                override fun onError(error: String) {
                    showToast(error)
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(croppedImageUri!!)
    }

    private fun moveToResult(classificationResult: String, classificationConfident: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, croppedImageUri.toString())
        intent.putExtra(ResultActivity.EXTRA_LABEL_RESULT, classificationResult)
        intent.putExtra(ResultActivity.EXTRA_CONF_SCORE, classificationConfident)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG_IMAGE = "ImageURI"
        private const val TAG_PHOTOPICKER = "PhotoPicker"
    }

}
