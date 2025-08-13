package com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class SubmittedAssignment (
    val id: String,
    val description: String,
    val submitted_on: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)