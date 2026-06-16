package com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListModel

data class GetHomeworkSubmissionListData (
    val id: String,
    val name: String,
    val admission_no: String,
    val roll_no: String,
    val status: String,
    val completed_on: String?
)
