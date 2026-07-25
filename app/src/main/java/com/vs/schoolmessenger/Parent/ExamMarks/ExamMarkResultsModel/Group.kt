package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel

data class Group(
    val name: String,
    val mark: String?,
    val subjects: List<Subgroup>
)