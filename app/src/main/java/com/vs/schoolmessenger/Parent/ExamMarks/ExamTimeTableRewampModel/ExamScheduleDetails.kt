package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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

) : Parcelable