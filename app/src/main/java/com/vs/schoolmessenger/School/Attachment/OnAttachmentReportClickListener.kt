package com.vs.schoolmessenger.School.Attachment

import android.view.View
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData


interface OnAttachmentReportClickListener {
    fun onItemClick(parentData: List<AttachmentReportData>, view: View, isPosition: Int)
}