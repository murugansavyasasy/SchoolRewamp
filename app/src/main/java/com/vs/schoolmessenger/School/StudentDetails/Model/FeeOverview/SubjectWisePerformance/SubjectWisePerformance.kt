package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.SubjectWisePerformance

data class SubjectWisePerformance(
    val strongestSubject: String?,
    val weakestSubject: String?,
    val subjects: List<Subject>?
)