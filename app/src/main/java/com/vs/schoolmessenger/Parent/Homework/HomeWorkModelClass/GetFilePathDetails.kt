package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class GetFilePathDetails(
    val type: String,
    val url: String
) : Parcelable