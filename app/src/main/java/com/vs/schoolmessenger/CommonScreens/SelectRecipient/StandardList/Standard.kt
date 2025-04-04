package com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section

data class Standard(@SerializedName("id") val id: Int,
                    @SerializedName("name") val name: String,
                    @SerializedName("sections") val sections: List<Section>)