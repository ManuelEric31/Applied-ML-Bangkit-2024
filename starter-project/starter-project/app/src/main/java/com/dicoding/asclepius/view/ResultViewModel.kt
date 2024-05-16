package com.dicoding.asclepius.view

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.repository.HistoryRepository

class ResultViewModel(application: Application): ViewModel() {
    private val mHistoryUserRepository: HistoryRepository = HistoryRepository(application)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getHistoryByUri(uri: String): LiveData<HistoryUser> {
        return mHistoryUserRepository.getHistoryByUri(uri)
    }

    fun insert(history: HistoryUser) {
        Log.d(TAG, "Inserting value history user: $history")
        mHistoryUserRepository.insert(history)
    }

    fun delete(history: HistoryUser) {
        mHistoryUserRepository.delete(history)
    }

    companion object {
        private const val TAG = "History Repository"
    }
}