package com.vs.schoolmessenger.Parent.Timetable

data class TimeTableResponse(
    val status: Boolean,
    val message: String,
    val data: List<TimeTableListData>
)