package com.vs.schoolmessenger.School.Homework.HomeWorkReportModel

data class HomeWorkReport(
    val title: String,
    val id: String,
    val description: String,
    val subject_name: String,
    val created_by: String,
    val file_path: List<FilePath>
)