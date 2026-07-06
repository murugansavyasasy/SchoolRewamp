package com.vs.schoolmessenger.School.ClassTest.Class.Models

data class  ClassTestItem(
    val subjectId: String,
    val subjectName: String,
    val sectionId: String,
    val sectionLabel: String,
    val isMerged: Boolean,
    val mergedSections: List<String>,
    val mergedSectionIds: List<String>,
    val tests: MutableList<TestEntry> = mutableListOf(),
    var isExpanded: Boolean = false
)