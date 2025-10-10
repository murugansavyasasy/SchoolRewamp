package com.vs.schoolmessenger.School.Attachment.DataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttachmentDataReport(
    val id: String,
    val title: String,
    val description: String,
    val school_id: String,
    val recipient_type: String,
    val date: String,
    val iframe: String,
    val file_size: String,
    val sent_by: String,
    val thumbnail: String,
    var is_unread: Boolean,
    val can_edit: Boolean,
    val can_delete: Boolean,
    val file_path: List<AttachmentFilePath>
): Parcelable
