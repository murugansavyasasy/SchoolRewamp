package com.vs.schoolmessenger.School.MarkYourAttendance.Interface

interface LocationLatLongListener {
    fun onLocationReturn(latitude: Double, longitude: Double, type: String?)

}