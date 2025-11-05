package com.vs.schoolmessenger.Dashboard.NewFeatures.Model

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class GetFeatureData (
    val id: String,
    val title: String,
    val description: String,
    val file_path: List<AttachmentFile>

)