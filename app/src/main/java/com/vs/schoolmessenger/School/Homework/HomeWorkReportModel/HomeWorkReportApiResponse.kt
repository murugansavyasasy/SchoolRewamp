package com.vs.schoolmessenger.School.Homework.HomeWorkReportModel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeWorkReportApiResponse(
    val status: Boolean,
    val message: String,
    val data: List<HomeWorkReportData>
) : Parcelable