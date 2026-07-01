package com.vs.schoolmessenger.School.ClassTest.Class.Models

data class SectionDataDetail(
    val section_id: String,
    val section_name: String,
    val subjects: List<SubjectDetail>
)
