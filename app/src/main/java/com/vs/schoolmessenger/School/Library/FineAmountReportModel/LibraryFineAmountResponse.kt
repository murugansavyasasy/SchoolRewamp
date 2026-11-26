package com.vs.schoolmessenger.School.Library.FineAmountReportModel

import com.google.gson.annotations.SerializedName

data class LibraryFineAmountResponse(

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<LibraryFineAmountData>
)