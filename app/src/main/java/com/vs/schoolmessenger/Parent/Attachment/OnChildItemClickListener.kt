package com.vs.schoolmessenger.Parent.Attachment

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

interface OnChildItemClickListener {
    fun onChildItemClick(file: AttachmentFile, parentData: AttachmentData)
}
