package com.vs.schoolmessenger.School.MarkYourAttendance.Interface

import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryData

interface LocationHistoryClickListener {
    fun onItemClick(data: LocationHistoryData, isType: String)
}