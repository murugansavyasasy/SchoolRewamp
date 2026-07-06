package com.vs.schoolmessenger.School.ClassTest.Class.Models

data class TestEntry(
    var examName: String = "",
    var testDate: String = "",
    var session: String = "FN",
    var maxMarks: String = "100",
    var minMarks: String = "35",
    var syllabus: String = "",
    var classTestSubjectId: String? = null
)