package com.vs.schoolmessenger.School.MessageFromManagement.Model

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class GetMessagesStaffData (
    val type: String,
    val id: String,
    val school_id: String,
    val header_id: String,
    val title: String?,
    val school_name: String?,
    val role: String?,
    val description: String?,
    val content: String?,
    val file_path: List<AttachmentFile>?,
    val iframe: String?,
    val file_size: String?,
    val thumbnail: String?,
    val date: String?,
    val time: String?,
    val sent_by: String?,
    val sender_info: String?,
    val is_unread: Boolean,
    val is_emergency: Boolean,
    val duration: Int?,
    val is_archive: Boolean
)
