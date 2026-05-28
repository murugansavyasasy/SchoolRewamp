package com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel

import com.vs.schoolmessenger.Parent.RequestLeave.getCatorgiesData

class GetLeaveCategoriesData(
    val status: Boolean,
    val message: String,
    val data: List<getCatorgiesData>
)