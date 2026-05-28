package com.vs.schoolmessenger.Parent.Attachment.Model

data class AttachmentResponse(
    val status: Boolean,
    val message: String,
    val data: List<AttachmentData>
)