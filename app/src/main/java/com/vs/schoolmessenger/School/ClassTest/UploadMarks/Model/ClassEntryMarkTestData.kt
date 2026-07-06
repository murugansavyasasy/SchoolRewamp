package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model

import com.google.gson.annotations.SerializedName

data class ClassEntryMarkTestData (
    @SerializedName("class_test_id")
    val classTestId: String,

    @SerializedName("section_id")
    val sectionId: String,

    @SerializedName("subjects")
    val subjects: List<SubjectMarksEntryData>
)