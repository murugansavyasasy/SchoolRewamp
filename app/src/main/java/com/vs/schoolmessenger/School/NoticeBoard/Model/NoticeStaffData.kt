package com.vs.schoolmessenger.School.NoticeBoard.Model

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.Noticeboard.FilePath
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoticeStaffData(
    val id: String,
    val title: String,
    val description: String,
    val created_on: String,
    val day: String,
    val visible_from: String,
    val visible_to: String,
    val intended_for: String,
    val is_management: Boolean,
    val can_edit: Boolean,
    val can_delete: Boolean,
    val iframe: String,
    val sent_by: String,
    val file_path: List<FilePath>
) : Parcelable