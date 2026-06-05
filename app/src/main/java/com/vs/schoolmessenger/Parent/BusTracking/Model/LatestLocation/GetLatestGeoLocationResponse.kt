package com.vs.schoolmessenger.Parent.BusTracking.Model.LatestLocation

data class GetLatestGeoLocationResponse (
    val status : Boolean,
    val message : String,
    val data : List<VehicleLocationData>
)