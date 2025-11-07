package com.vs.schoolmessenger.Auth.Introduction.Model

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class GetFeatureData (
    val id: String,
    val title: String,
    val description: String,
    val file_path: List<AttachmentFile>

)