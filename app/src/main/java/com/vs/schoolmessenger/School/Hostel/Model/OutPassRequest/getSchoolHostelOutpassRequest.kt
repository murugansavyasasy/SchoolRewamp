package com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest


class getSchoolHostelOutpassRequest(
    val status: Boolean,
    val message: String,
    val data: List<OutpassRequestWiseData>
)