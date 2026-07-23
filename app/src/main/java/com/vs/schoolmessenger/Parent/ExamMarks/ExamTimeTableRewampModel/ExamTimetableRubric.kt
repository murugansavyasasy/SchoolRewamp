package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamTimetableRubric(

    @SerializedName("rubric_id")
    val rubricId: Int?,

    @SerializedName("rubric_name")
    val rubricName: String?,

    @SerializedName("scheduling_details")
    val schedulingDetails: ExamScheduleDetails?

) : Parcelable