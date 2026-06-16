package com.vs.schoolmessenger.School.LSRW.Model

import android.os.Parcelable
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import kotlinx.parcelize.Parcelize

@Parcelize
data class LsrwTask(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val sent_to: String,
    val activity_type: String,
    val created_on: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val submitted_average: String,
    val submission_date: String,
    val can_edit: Boolean,
    val can_delete: Boolean,
    val file_path: List<FilePath>
) : Parcelable