package com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffApplyLeaveRequest

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

class StaffLeaveRequestApplyRespone (
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Any>
)


