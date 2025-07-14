package com.vs.schoolmessenger.Parent.ExamMarks.Model

data class ExamData (
    val id: String,
    val name: String,
    val description: String,
    val created_on: String,
    val exam_subject_details: List<ExamSubjectDetail>
)