package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class ExamSectionData(
    val exam_section_id: String,
    val exam_id: String?,
    val upload_details: List<StudentMarkApi>
)
