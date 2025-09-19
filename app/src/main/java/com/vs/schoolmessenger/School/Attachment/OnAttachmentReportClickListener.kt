package com.vs.schoolmessenger.School.Attachment

import android.view.View
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport


interface OnAttachmentReportClickListener {
    fun onItemClick(isData: List<AttachmentDataReport>, view: View, isPosition: Int)
    fun onReadStatusClick(isData: List<AttachmentDataReport>, isPosition: Int)
}