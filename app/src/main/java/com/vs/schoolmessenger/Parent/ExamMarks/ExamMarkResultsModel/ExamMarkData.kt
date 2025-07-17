package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel

data class ExamMarkData (
    val subject_marks: List<SubjectMark>,
    val assessments: List<Assessment>,
    val groups: List<Group>,
    val is_unread: Boolean
)