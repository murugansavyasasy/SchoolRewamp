package com.vs.schoolmessenger.Parent.LSRW.Model
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath

data class SkillData(
    val id: String,
    val detail_id: String,
    val title: String,
    val description: String,
    val activity_type: String,
    val subject: String,
    val date: String,
    val time: String,
    val submitted_date: String,
    val is_submitted: Boolean,
    val is_unread: Boolean,
    val sent_by: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)
