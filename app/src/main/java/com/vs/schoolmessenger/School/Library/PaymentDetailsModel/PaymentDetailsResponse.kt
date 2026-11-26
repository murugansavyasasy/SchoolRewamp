package com.vs.schoolmessenger.School.Library.PaymentDetailsModel

import com.google.gson.annotations.SerializedName

data class PaymentDetailsResponse (

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<String>
)