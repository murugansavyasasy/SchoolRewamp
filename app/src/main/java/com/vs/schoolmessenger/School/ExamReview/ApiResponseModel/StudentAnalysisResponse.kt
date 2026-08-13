package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.MarkData

data class StudentAnalysisResponse (
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<StudentAnalysisData>
)