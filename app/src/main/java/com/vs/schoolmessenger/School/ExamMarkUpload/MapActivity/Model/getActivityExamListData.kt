package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model


data class getActivityExamListData (
    val title: String,
    val month: String,
    val subjects: List<getActivitySubjectData>,
)