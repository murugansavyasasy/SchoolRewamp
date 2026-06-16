package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData

interface AssignmentClickListener {
    fun onSubmittedClick(data: AssignmentData)
    fun onEditAndDeleteClick(data: AssignmentData, anchorView: View, adapterPosition: Int)
    fun onNotSubmittedClick(data: AssignmentData)

    fun onItemClick(data: AssignmentData, holder: AssignmentAdapter.DataViewHolder)

    fun onReadStatusClick(isData: ParentAssignmentData, isPosition: Int)


    fun onClickListener(data: SubmittedAssignment, anchorView: View, adapterPosition: Int)


}