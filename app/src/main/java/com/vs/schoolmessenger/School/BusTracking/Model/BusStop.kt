package com.vs.schoolmessenger.School.BusTracking.Model

data class BusStop(
    val id: String,
    val name: String,
    val time: String,
    val lat: Double,
    val lng: Double,
    var isCompleted: Boolean = false,
    var isCurrent: Boolean = false
)