package com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel

data class AvgStudentSubmission (
    val id: String,
    val title: String,
    val description: String,
    val activity_type: String,
    val submitted_average: String,
    val submission_date: String,
    val submitted_count: Int,
    val student_id: String,
    val student_name: String,
    val remark: String,
    val std_sec: String,
    val student_submited_on: String,
    val is_submitted: Boolean
)