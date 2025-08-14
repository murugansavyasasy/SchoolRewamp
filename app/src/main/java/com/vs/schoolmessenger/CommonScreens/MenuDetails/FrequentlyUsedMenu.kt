package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class FrequentlyUsedMenu (
    val id: Int,
    val name: String,
    val description: String,
)