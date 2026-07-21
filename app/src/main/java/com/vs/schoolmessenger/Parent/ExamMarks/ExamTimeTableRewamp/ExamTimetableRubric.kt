package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamTimetableRubric(

    @SerializedName("rubric_id")
    val rubricId: Int?,

    @SerializedName("rubric_name")
    val rubricName: String?,

    @SerializedName("scheduling_details")
    val schedulingDetails: ExamScheduleDetails?
)