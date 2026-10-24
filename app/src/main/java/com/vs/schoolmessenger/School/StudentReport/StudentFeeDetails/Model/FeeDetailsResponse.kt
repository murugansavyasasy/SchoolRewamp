package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class FeeDetailsResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<FeeDetailsData>? = null
)