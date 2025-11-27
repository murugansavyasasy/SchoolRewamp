package com.vs.schoolmessenger.School.LSRW.SubmissionStudentListModel
import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class StudentSubmissionLsrw(
    val id: String,
    val submission_id: String,
    val student_id: String,
    val created_on: String,
    val student_name: String,
    val standard: String,
    val section: String,
    val mobile_no: String,
    val submit_status: String,
    val submitted_date: String,
    val iframe: String?,
    val file_size: String?,
    val thumbnail: String?,
    val file_path: List<FilePath> = emptyList()
)

