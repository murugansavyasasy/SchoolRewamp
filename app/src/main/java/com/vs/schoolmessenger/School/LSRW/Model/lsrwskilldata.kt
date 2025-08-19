package com.vs.schoolmessenger.School.LSRW.Model
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class lsrwskilldata(
    val id: String,
    val title: String,
    val description: String,
    val sent_to: String,
    val activity_type: String,
    val created_on: String,
    val file_path: List<FilePath>,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val submitted_average: String
)