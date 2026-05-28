package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Enum.SortField
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Enum.SortOrder

data class SortConfig(
    val field: SortField,
    val order: SortOrder
)
