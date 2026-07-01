package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class MarkActivityData(
    @SerializedName("class_test_subject_id") val classTestSubjectId: String,
    @SerializedName("exam_date") val examDate: String,
    @SerializedName("session") val session: String,
    @SerializedName("activity_name") val activityName: String,
    @SerializedName("max_mark") val maxMark: String,
    @SerializedName("min_mark") val minMark: String,
    @SerializedName("syllabus") val syllabus: String,
    @SerializedName("attendance") val attendance: String,
    @SerializedName("mark") val mark: String,
    @SerializedName("remarks") val remarks: String
)