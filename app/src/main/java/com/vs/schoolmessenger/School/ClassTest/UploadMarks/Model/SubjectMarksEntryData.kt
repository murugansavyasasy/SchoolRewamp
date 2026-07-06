package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model

import com.google.gson.annotations.SerializedName

data class SubjectMarksEntryData (
    @SerializedName("subject_id")
    val subjectId: String,

    @SerializedName("subject_name")
    val subjectName: String,

    @SerializedName("activities")
    val activities: List<ActivityDataMarkClassEntry>
)