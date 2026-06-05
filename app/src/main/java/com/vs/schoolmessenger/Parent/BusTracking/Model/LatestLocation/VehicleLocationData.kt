package com.vs.schoolmessenger.Parent.BusTracking.Model.LatestLocation

data class VehicleLocationData (
    val id: String,
    val device_id: String,
    val vehicle_id: String,
    val latitude: String,
    val longitude: String,
    val speed: String,
    val altitude_level: String,
    val vehicle_direction: String,
    val gps_time: String
)