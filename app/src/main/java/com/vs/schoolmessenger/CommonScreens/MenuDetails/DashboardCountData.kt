package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class DashboardCountData (
    @SerializedName(APIKeyNames.contact_details) val contactDetails: ContactDetails,
    @SerializedName(APIKeyNames.menu_details) val menu_details: ArrayList<MenuCountDetail>
)