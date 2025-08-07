package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData

interface AssignmentClickListener {
    fun onSubmittedClick(data: AssignmentData)
    fun onEditAndDeleteClick(data: AssignmentData, anchorView: View, adapterPosition: Int)
    fun onNotSubmittedClick(data: AssignmentData)
}