package com.vs.schoolmessenger.School.Hostel.Model.HostelList


data class getHostelList (
    val status: Boolean,
    val message: String,
    val data: List<getHostelListData>
)