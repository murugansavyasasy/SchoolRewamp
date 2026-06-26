package com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass

data class SectionDataDetail (
    val section_id: String,
    val section_name: String,
    val subjects: List<SubjectDataDetail>
)