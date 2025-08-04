package com.vs.schoolmessenger.School.Attachment

import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData


interface OnAttachmentReportClickListener {
    fun onItemClick(parentData: AttachmentReportData)
}