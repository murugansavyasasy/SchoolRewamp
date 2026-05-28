package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class DashboardData(
    val is_birthday: Boolean,
    @SerializedName(APIKeyNames.contact_details) val contactDetails: ContactDetails,
    @SerializedName(APIKeyNames.frequently_used) val frequently_used: List<MenuDetail>,
    @SerializedName(APIKeyNames.menus) val menus: List<MenuDetail>
)