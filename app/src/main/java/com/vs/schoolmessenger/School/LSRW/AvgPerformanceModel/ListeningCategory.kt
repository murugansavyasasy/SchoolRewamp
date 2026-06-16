package com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel

data class ListeningCategory(
    val over_all_percentage: String,
    val student_count: String,
    val details: List<AvgStudentSubmission>
)