package com.dicoding.asclepius.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.asclepius.database.HistoryDao
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.database.HistoryUserDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HistoryRepository(application: Application) {
    private val mHistoryUserDao: HistoryDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = HistoryUserDatabase.getDatabase(application)
        mHistoryUserDao = db.historyUserDao()
    }

    fun getAllHistoryUser(): LiveData<List<HistoryUser>> = mHistoryUserDao.getAllHistoryUser()
    fun getHistoryByUri(uri: String): LiveData<HistoryUser> =
        mHistoryUserDao.getHistoryUserByUri(uri)

    fun insert(history: HistoryUser) {
        executorService.execute { mHistoryUserDao.insert(history) }
    }

    fun delete(favorite: HistoryUser) {
        executorService.execute { mHistoryUserDao.delete(favorite) }
    }

}