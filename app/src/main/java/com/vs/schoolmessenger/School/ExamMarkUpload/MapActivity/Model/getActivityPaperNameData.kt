package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

data class getActivityPaperNameData(
    var activity_id: String,
    val name: String,
    val activities: List<String>,
    var selectedValue: String? = null,
    var selectedActivityID: String? = null
)