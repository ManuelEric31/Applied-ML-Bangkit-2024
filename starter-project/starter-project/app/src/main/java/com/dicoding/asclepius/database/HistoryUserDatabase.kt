package com.dicoding.asclepius.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryUser::class], version = 1)
abstract class HistoryUserDatabase: RoomDatabase() {
    abstract fun historyUserDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HistoryUserDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): HistoryUserDatabase {
            if (INSTANCE == null) {
                synchronized(HistoryUserDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        HistoryUserDatabase::class.java, "history_user_database"
                    )
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE as HistoryUserDatabase
        }
    }
}