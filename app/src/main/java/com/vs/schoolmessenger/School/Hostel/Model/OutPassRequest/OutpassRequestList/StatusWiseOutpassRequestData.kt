package com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList

data class StatusWiseOutpassRequestData (
    val status: String,
    val attd_details: List<OutpassRequestWiseData>
)