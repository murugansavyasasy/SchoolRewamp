package com.vs.schoolmessenger.Parent.Assignment.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class FilePath(
        val url: String,
    val type: String
) : Parcelable


//data class FilePath(
//    val url: String,
//    val type: String
//)
