package com.vs.schoolmessenger.School.MessageFromManagement.Model

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath

data class GetMessagesStaffData (
    val type: String,
    val id: String,
    val header_id: String,
    val title: String?,
    val description: String?,
    val content: String?,
    val file_path: List<FilePath>?,
    val iframe: String?,
    val file_size: String?,
    val thumbnail: String?,
    val date: String?,
    val time: String?,
    val sender_info: String?,
    val is_unread: Boolean,
    val duration: Int?,
    val is_archive: Boolean
)
