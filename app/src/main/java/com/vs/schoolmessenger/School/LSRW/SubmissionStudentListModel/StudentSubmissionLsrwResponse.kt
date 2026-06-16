package com.vs.schoolmessenger.School.LSRW.SubmissionStudentListModel

data class StudentSubmissionLsrwResponse(
    val status: Boolean,
    val message: String,
    val data: List<StudentSubmissionLsrw>
)