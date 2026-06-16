package com.vs.schoolmessenger.Parent.Attachment.Model

import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter

interface AttachmentClickListener {
    fun onItemClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder)
    fun onSeeMoreClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder)
    fun onSearchResultEmpty(isEmpty: Boolean)

}