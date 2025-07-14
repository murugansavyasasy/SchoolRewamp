package com.vs.schoolmessenger.Parent.ExamMarks.Model

data class ExamSubjectDetail (
    val id: String,
    val subject_name: String,
    val exam_date: String,
    val exam_session: String,
    val max_mark: String,
    val syllabus: String
)