package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class NameAndIds(
    @SerializedName(APIKeyNames.id) val id: Int,
    @SerializedName(APIKeyNames.name) val name: String,
    @SerializedName(APIKeyNames.admission_no) val admission_no: String,
    @SerializedName(APIKeyNames.roll_no) val roll_no: String,
    @SerializedName(APIKeyNames.created_on) val created_on: String

)
