package com.dicoding.asclepius.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class HistoryUser (
    @PrimaryKey(autoGenerate = false)

    var category: String = "",
    var confident: String? = null,
    var uriImage: String? = null
) : Parcelable