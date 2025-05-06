package com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class Section(@SerializedName(APIKeyNames.id) val id: Int,
                   @SerializedName(APIKeyNames.name) val name: String)