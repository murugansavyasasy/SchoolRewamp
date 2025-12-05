package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model

data class getExamListData(
    val title: String,
    val month: String,
    val subjects: List<getSubjectData>,
)