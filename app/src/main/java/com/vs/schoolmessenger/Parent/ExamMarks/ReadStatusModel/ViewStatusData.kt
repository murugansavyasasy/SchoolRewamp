package com.vs.schoolmessenger.Parent.ExamMarks.ReadStatusModel

data class ViewStatusData(
    val report_id: Int,
    val view_type: String,
    val mark_viewed: Boolean
)