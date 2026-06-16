package com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel

import android.os.Parcelable
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import kotlinx.parcelize.Parcelize


@Parcelize
data class SubmittedAssignment(
    val id: String,
    val title: String,
    val description: String,
    val submitted_on: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
) : Parcelable