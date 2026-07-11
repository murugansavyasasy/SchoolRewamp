package com.vs.schoolmessenger.School.ClassTest.Class.Models

data class TestEntry(
    var examName: String = "",
    var testDate: String = "",
    var session: String = "FN",
    var maxMarks: String = "",
    var minMarks: String = "",
    var syllabus: String = "",
    var classTestSubjectId: String? = null,
    var canDelete: Boolean = true
)