package com.vs.schoolmessenger.Parent.Attachment

import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkDateData

interface AttachmentClickListener {
    fun onItemClick(data: AttachmentData, holder: AttachmentAdapter.DataViewHolder)


}