package com.dicoding.asclepius.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History (
    var category: String = "",
    var confident: String? = null,
    var uriImage: String? = null
) : Parcelable
