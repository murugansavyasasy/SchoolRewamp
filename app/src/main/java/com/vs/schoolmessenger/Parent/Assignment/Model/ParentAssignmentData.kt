package com.vs.schoolmessenger.Parent.Assignment.Model

import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class ParentAssignmentData(
    val id: String,
    val header_id: String,
    val title: String,
    val description: String,
    val category: String,
    val created_date: String,
    val subject: String,
    val date: String,
    val time: String,
    val submitted_count: Int,
    val end_date: String,
    val is_unread: Boolean,
    val sent_by: String,
    val sort_order: String,
    val is_archive: Boolean,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)