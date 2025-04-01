package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail

data class DashboardData(
    @SerializedName("contact_details") val contactDetails: ContactDetails,
    @SerializedName("menu_details") val menuDetails: List<MenuDetail>
)