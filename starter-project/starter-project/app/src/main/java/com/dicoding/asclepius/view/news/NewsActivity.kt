package com.dicoding.asclepius.view.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.databinding.ActivityNewsBinding
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.response.ArticlesItem
import com.dicoding.asclepius.view.DataAdapter


class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private val resultViewModel: NewsViewModel by lazy {
        ViewModelProvider(this)[NewsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvCancerNews.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvCancerNews.addItemDecoration(itemDecoration)

        resultViewModel.articlesItem.observe(this) { newsArticle ->
            if (newsArticle != null && newsArticle.isNotEmpty()) {
                setReviewData(newsArticle)
            }
            else {
                showToast(getString(R.string.failed_to_loaded))
            }
        }

        resultViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setReviewData(newsArticle: List<ArticlesItem>) {
        val adapter = DataAdapter(this)
        adapter.submitList(newsArticle)
        binding.rvCancerNews.adapter = adapter
    }
}