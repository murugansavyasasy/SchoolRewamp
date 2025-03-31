package com.vs.schoolmessenger.Dashboard.School

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("contact_details") val contactDetails: ContactDetails,
    @SerializedName("menu_details") val menuDetails: List<MenuDetail>
)
