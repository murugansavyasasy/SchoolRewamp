package com.vs.schoolmessenger.School.Attachment.DataClass

data class AttachmentReportData(   val id: String,
                                   val title: String,
                                   val description: String,
                                   val recipient_type: String,
                                   val date: String,
                                   val iframe: String,
                                   val file_size: String,
                                   val sent_by: String,
                                   val thumbnail: String,
                                   val can_edit: Boolean,
                                   val can_delete: Boolean,
                                   val file_path: List<AttachmentFilePath>)
