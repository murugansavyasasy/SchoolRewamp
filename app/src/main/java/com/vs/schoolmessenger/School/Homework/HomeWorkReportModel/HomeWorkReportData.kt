package com.vs.schoolmessenger.School.Homework.HomeWorkReportModel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeWorkReportData(
    val title: String,
    val id: String,
    val description: String,
    val subject_name: String,
//    val created_by: String,
    val file_path: List<FilePath>
)  : Parcelable