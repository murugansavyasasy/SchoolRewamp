package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model

import com.google.gson.annotations.SerializedName

data class ActivityDataMarkClassEntry (
    @SerializedName("class_test_subject_id")
    val classTestSubjectId: String,

    @SerializedName("activity_name")
    val activityName: String,

    @SerializedName("exam_date")
    val examDate: String,

    @SerializedName("session")
    val session: String,

    @SerializedName("max_mark")
    val maxMark: String,

    @SerializedName("min_mark")
    val minMark: String,

    @SerializedName("syllabus")
    val syllabus: String,

    @SerializedName("students")
    val students: List<StudentMarkEntryUploadData>
)