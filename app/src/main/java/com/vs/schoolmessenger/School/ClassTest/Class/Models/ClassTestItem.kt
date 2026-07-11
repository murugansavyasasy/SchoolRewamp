package com.vs.schoolmessenger.School.ClassTest.Class.Models

data class ClassTestItem(
    val subjectId: String,
    val subjectName: String,
    val sectionId: String,
    val sectionLabel: String,
    var isMerged: Boolean = false,
    var mergedSections: List<String> = emptyList(),
    var mergedSectionIds: List<String> = emptyList(),
    var isExpanded: Boolean = false,
    var mergedSourceSnapshot: String = "",
    val tests: MutableList<TestEntry> = mutableListOf()
)