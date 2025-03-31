package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName

data class ContactDetails(
    @SerializedName("alert_message") val alertMessage: String,
    @SerializedName("alert_content") val alertContent: String,
    @SerializedName("alert_title") val alertTitle: String,
    @SerializedName("display_name") val displayName: String,
    val numbers: String,
    @SerializedName("button_content") val buttonContent: String
)