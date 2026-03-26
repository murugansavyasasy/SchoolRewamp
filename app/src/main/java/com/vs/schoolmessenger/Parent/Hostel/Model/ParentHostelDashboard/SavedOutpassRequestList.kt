package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard

import java.io.Serializable


data class SavedOutpassRequestList(
    val reason: String,
    val fromdate_todate: String,
    val request_time: String,
    val status: String
): Serializable