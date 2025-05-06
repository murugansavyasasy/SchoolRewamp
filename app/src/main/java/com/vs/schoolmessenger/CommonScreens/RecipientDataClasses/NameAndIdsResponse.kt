package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.android.gms.common.api.Api
import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class NameAndIdsResponse(
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<NameAndIds>
)
