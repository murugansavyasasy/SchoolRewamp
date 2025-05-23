package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.Repository.APIKeyNames

data class HolidayResponse(
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Holiday>
)


