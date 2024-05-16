package com.dicoding.asclepius.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)

    fun insert(favorite:HistoryUser)

    @Delete
    fun delete(favorite:HistoryUser)

    @Query("SELECT * from HistoryUser")
    fun getAllHistoryUser() : LiveData<List<HistoryUser>>

    @Query("SELECT * FROM HistoryUser WHERE uriImage = :uriImage")
    fun getHistoryUserByUri(uriImage: String): LiveData<HistoryUser>
}