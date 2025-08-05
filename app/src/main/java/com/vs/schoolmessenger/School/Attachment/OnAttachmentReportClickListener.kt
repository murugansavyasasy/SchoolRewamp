package com.vs.schoolmessenger.School.Attachment

import android.view.View
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData


interface OnAttachmentReportClickListener {
    fun onItemClick(isData: List<AttachmentReportData>, view: View, isPosition: Int)
    fun onReadStatusClick(isData: List<AttachmentReportData>,isPosition: Int)
}