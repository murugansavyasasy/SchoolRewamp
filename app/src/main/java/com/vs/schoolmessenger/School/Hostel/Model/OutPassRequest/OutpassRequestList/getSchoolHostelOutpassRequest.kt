package com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList

import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.StatusWiseOutpassRequestData

class getSchoolHostelOutpassRequest(
    val status: Boolean,
    val message: String,
    val data: List<StatusWiseOutpassRequestData>
)