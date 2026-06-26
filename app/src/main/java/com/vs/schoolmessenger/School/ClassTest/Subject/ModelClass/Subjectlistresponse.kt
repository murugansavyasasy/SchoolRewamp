package com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass

data class Subjectlistresponse (
    val status: Boolean,
    val message: String,
    val data: List<SectionDataDetail>
)