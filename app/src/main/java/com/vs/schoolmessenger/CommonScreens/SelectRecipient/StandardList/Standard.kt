package com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.Repository.APIKeyNames

data class Standard(
    @SerializedName(APIKeyNames.id) val id: Int,
    @SerializedName(APIKeyNames.name) val name: String,
    @SerializedName(APIKeyNames.sections) val sections: List<Section>
)