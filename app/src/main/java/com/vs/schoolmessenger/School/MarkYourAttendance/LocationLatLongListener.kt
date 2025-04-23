package com.vs.schoolmessenger.School.MarkYourAttendance

interface LocationLatLongListener {
    fun onLocationReturn(latitude: Double, longitude: Double, type: String?)

}