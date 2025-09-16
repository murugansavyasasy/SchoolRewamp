package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

class ProfileUpdateResponse  (
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Any>
)