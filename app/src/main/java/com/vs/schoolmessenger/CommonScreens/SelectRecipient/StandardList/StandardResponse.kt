package com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList

import com.google.gson.annotations.SerializedName

data class StandardResponse(@SerializedName("status") val status: Boolean,
                            @SerializedName("message") val message: String,
                            @SerializedName("data") val data: List<Standard>)