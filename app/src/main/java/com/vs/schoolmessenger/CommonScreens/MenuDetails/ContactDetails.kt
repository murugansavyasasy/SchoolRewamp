package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class ContactDetails(
    @SerializedName(
        APIKeyNames.alert_message) val alertMessage: String,
    @SerializedName(APIKeyNames.alert_content) val alertContent: String,
    @SerializedName(APIKeyNames.alert_title) val alertTitle: String,
    @SerializedName(APIKeyNames.display_name) val displayName: String,
    val numbers: String,
    @SerializedName(APIKeyNames.button_content) val buttonContent: String
)