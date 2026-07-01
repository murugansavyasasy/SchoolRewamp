package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class ClassTestData(

    @SerializedName("class_test_id")
    val classTestId: String,

    @SerializedName("exam_name")
    val examName: String,

    @SerializedName("subjects")
    val subjects: List<TestSubjectData>
)