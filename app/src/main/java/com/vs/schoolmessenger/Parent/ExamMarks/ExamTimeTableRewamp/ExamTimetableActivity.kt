package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamTimetableActivity(

    @SerializedName("activity_id")
    val activityId: Int?,

    @SerializedName("activity_name")
    val activityName: String?,

    @SerializedName("scheduling_details")
    val schedulingDetails: ExamScheduleDetails?,

    @SerializedName("rubrics")
    val rubrics: List<ExamTimetableRubric>?
)