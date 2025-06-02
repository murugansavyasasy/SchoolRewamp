package com.vs.schoolmessenger.Parent.Attachment

data class AttachmentResponse(
    val status: Boolean,
    val message: String,
    val data: List<AttachmentData>
)