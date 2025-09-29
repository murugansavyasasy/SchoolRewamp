package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData

interface AssignmentClickListener {
    fun onSubmittedClick(data: AssignmentData)
    fun onEditAndDeleteClick(data: AssignmentData, anchorView: View, adapterPosition: Int)
    fun onNotSubmittedClick(data: AssignmentData)

    fun onItemClick(data: AssignmentData, holder: AssignmentAdapter.DataViewHolder)

    fun onReadStatusClick(isData: ParentAssignmentData, isPosition: Int)


    fun onClickListener(data: SubmittedAssignment, anchorView: View, adapterPosition: Int)



}