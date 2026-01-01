package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class IssueSummary(
    var total: Int = 0,
    var absentCount: Int = 0,
    var maxMarkCount: Int = 0,
    var systemMsgCount: Int = 0,
    var invalidCount: Int = 0
)

