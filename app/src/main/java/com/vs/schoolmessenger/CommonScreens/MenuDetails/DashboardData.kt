package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Repository.APIKeyNames

data class DashboardData(
    @SerializedName(APIKeyNames.contact_details) val contactDetails: ContactDetails,
    @SerializedName(APIKeyNames.frequently_used) val frequently_used: List<FrequentlyUsedMenu>,
    @SerializedName(APIKeyNames.menus) val menus: List<MenuDetail>
    )