package com.vs.schoolmessenger.Parent.Assignment

import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData

interface AssignmentClickListener {
    fun onSubmittedClick(data: AssignmentData)
    fun onDeleteClick(data: AssignmentData)
    fun onNotSubmittedClick(data: AssignmentData)
}