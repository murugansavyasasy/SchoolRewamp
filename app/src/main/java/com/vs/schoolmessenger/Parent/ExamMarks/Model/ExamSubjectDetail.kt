package com.vs.schoolmessenger.Parent.ExamMarks.Model

data class ExamSubjectDetail(
    val id: String,
    val subject_name: String,
    val exam_date: String,
    val exam_session: String,
    val start_time: String,
    val end_time: String,
    val max_mark: String,
    val syllabus: String
)