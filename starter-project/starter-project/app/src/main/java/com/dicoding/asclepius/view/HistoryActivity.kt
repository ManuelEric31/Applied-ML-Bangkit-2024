package com.dicoding.asclepius.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.factory.ViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyViewModel = obtainViewModel(this@HistoryActivity)

        binding.rvCheckUpHistory.layoutManager = LinearLayoutManager(this)
        historyAdapter =
            HistoryAdapter(emptyList()) { historyUser ->
                moveToResultActivity(historyUser)
            }
        binding.rvCheckUpHistory.adapter = historyAdapter

        historyViewModel.getAllFavoriteUser().observe(this) { historyList ->
            historyAdapter.updateData(historyList)
        }
    }

    private fun obtainViewModel(activity: AppCompatActivity): HistoryViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(HistoryViewModel::class.java)
    }

    private fun moveToResultActivity(historyUser: HistoryUser) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, historyUser.uriImage)
            putExtra(ResultActivity.EXTRA_CONF_SCORE, historyUser.confident)
            putExtra(ResultActivity.EXTRA_LABEL_RESULT, historyUser.category)
        }
        startActivity(intent)
    }
}


