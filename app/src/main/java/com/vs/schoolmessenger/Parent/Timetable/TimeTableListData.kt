package com.vs.schoolmessenger.Parent.Timetable

data class TimeTableListData (
    val name: String,
    val start_time: String,
    val end_time: String,
    val duration: String,
    val order: String,
    val hour_type: String,
    val subject_name: String,
    val staff_name: String,
)
