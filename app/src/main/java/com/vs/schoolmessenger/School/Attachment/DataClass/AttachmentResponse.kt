package com.vs.schoolmessenger.School.Attachment.DataClass

data class AttachmentReportResponse(   val status: Boolean,
                                 val message: String,
                                 val data: List<AttachmentReportData>)
