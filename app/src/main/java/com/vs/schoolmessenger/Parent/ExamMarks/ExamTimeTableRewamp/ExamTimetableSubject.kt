package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamTimetableSubject(

    @SerializedName("subject_id")
    val subjectId: Int?,

    @SerializedName("subject_name")
    val subjectName: String?,

    @SerializedName("activities")
    val activities: List<ExamTimetableActivity>?
)