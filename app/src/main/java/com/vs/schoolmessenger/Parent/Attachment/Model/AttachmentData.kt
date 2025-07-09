package com.vs.schoolmessenger.Parent.Attachment.Model

data class AttachmentData(
    val id: String,
    val title: String,
    val description: String,
    val iframe: String,
    val date: String,
    val time: String,
    val sender_info: String,
    var is_unread: Boolean,
    val is_archive: Boolean,
    val file_path: List<AttachmentFile>
)