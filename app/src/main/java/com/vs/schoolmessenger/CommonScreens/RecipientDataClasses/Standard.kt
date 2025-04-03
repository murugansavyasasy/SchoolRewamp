package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName

data class Standard( @SerializedName("id") val id: Int,
                     @SerializedName("name") val name: String,
                     @SerializedName("sections") val sections: List<Section>)
