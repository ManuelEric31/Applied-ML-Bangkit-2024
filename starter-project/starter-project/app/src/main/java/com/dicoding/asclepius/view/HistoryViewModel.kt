package com.dicoding.asclepius.view

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.repository.HistoryRepository

class HistoryViewModel(application: Application) : ViewModel() {
    private val mHistoryUserRepository: HistoryRepository =
        HistoryRepository(application)

    fun getAllFavoriteUser(): LiveData<List<HistoryUser>> =
        mHistoryUserRepository.getAllHistoryUser()
}