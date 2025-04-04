package com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList

import com.google.gson.annotations.SerializedName

data class Section(@SerializedName("id") val id: Int,
                   @SerializedName("name") val name: String)