package com.vs.schoolmessenger.School.Homework.HomeWorkReportModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilePath(
    val url: String,
    val type: String
) : Parcelable