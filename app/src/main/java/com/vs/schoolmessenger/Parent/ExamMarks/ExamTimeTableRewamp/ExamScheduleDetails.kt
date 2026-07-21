package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamScheduleDetails(

    @SerializedName("date")
    val date: String?,

    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("session")
    val session: String?,

    @SerializedName("venue")
    val venue: String?,

    @SerializedName("syllabus")
    val syllabus: String?
)