package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class TestActivityData(

    @SerializedName("class_test_subject_id")
    val classTestSubjectId: String,

    @SerializedName("exam_date")
    val examDate: String,

    @SerializedName("session")
    val session: String,

    @SerializedName("activity_name")
    val activityName: String,

    @SerializedName("max_mark")
    val maxMark: String,

    @SerializedName("min_mark")
    val minMark: String,

    @SerializedName("syllabus")
    val syllabus: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("is_mark_uploaded")
    val isMarkUploaded: Boolean
)
