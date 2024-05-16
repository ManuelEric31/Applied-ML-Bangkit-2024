package com.dicoding.asclepius.view.news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.BuildConfig
import com.dicoding.asclepius.response.ArticlesItem
import com.dicoding.asclepius.response.HistoryResponse
import com.dicoding.asclepius.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsViewModel : ViewModel() {
    private val _articlesItem = MutableLiveData<List<ArticlesItem>>()
    val articlesItem: LiveData<List<ArticlesItem>> = _articlesItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        showNewsArticles()
    }

    private fun showNewsArticles() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getTopHeadlines(QUERY, CATEGORY, LANGUAGE, BuildConfig.TOKEN)
        client.enqueue(object : Callback<HistoryResponse> {
            override fun onResponse(
                call: Call<HistoryResponse>,
                response: Response<HistoryResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val articles = responseBody.articles?.filterNotNull()?.filter { !it.isRemoved() }
                        _articlesItem.value = articles
                    }
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private const val TAG = "NewsViewModel"
        private const val QUERY = "cancer"
        private const val CATEGORY = "health"
        private const val LANGUAGE = "en"
    }
}

