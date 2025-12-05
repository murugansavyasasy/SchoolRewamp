package com.vs.schoolmessenger.School.Attachment.DataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttachmentFilePath(
    val url: String,
    val type: String
) : Parcelable
