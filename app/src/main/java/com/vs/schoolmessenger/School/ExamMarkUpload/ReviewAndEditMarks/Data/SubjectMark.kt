package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class SubjectMark(
    val subject_id: String,
    val subject_name: String,
    val activities: List<ActivityMark>
)