package com.vs.schoolmessenger.Parent.Homework

import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData

interface HomeWorkDateClickListener {
    fun onItemClick(data: GetDateWiseHomeworkData, holder: HomeWorkAdapter.DataViewHolder)
}