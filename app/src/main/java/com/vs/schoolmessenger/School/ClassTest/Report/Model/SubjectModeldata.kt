package com.vs.schoolmessenger.School.ClassTest.Report.Model

import com.google.gson.annotations.SerializedName

data class SubjectModeldata (
    @SerializedName("subject_id")
    val subjectId: String,

    @SerializedName("subject_name")
    val subjectName: String,

    @SerializedName("activities")
    val activities: List<ActivityModeldata>
)