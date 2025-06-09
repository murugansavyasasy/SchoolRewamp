package com.vs.schoolmessenger.Parent.Attachment.Model

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class AttachmentData(
    val id: String,
    val title: String,
    val description: String,
    val iframe: String,
    val date: String,
    val time: String,
    val sender_info: String,
    val is_unread: Boolean,
    val is_archive: Boolean,
    val file_path: List<AttachmentFile>
)