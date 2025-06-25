package com.vs.schoolmessenger.Parent.Attachment.Model

import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter

interface AttachmentClickListener {
    fun onItemClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder)
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onUpdateArchiveStatus(type: String?, detailId: String?)
    fun onUpdateAttachmentStatus(type: String?, detailId: String?)

}