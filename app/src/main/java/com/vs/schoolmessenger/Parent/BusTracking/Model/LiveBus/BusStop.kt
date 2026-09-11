package com.vs.schoolmessenger.Parent.BusTracking.Model.LiveBus

data class BusStop(
    val id: String,
    val name: String,
    val time: String,
    val lat: Double,
    val lng: Double,
    var isCompleted: Boolean = false,
    var isCurrent: Boolean = false,
    var isFirst : Any,
    var isLast : Any,
    val isMyStop: Boolean = false
)