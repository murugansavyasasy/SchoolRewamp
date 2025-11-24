package com.vs.schoolmessenger.School.Assignment.DataClass

import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignmentData(
    val id: String,
    val header_id: String,
    val title: String,
    val description: String,
    val category: String,
    val subject: String,
    val created_date: String,
    val created_time: String,
    val submitted_count: Int,
    val recipient_type: String,
    val can_edit: Boolean,
    val can_delete: Boolean,
    val total_count: Int,
    val end_date: String,
    var is_unread: Boolean,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)  : Parcelable

