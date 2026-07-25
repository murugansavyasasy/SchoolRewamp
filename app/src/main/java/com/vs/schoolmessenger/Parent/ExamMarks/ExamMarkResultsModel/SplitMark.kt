package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel

data class SplitMark(
    val name: String,
    val max_mark: String,
    val mark_obtained: String,
    val rubrics: List<Rubric>?

)
data class Rubric(
    val name: String,
    val max_mark: String,
    val mark_obtained: String
)

