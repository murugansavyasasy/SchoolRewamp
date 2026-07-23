package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamTimetableActivity(

    @SerializedName("activity_id")
    val activityId: Int?,

    @SerializedName("activity_name")
    val activityName: String?,

    @SerializedName("max_mark")
    val max_mark: String?,

    @SerializedName("pass_mark")
    val pass_mark: String?,

    @SerializedName("scheduling_details")
    val schedulingDetails: ExamScheduleDetails?,

    @SerializedName("rubrics")
    val rubrics: List<ExamTimetableRubric>?

) : Parcelable