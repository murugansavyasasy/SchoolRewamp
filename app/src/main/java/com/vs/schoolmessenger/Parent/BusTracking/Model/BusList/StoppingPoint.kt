package com.vs.schoolmessenger.Parent.BusTracking.Model.BusList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoppingPoint (
    val vehicle_id: String,
    val route_name: String,
    val journey_type: String,
    val start_time: String,
    val end_time: String,
    val working_days: List<String>,
    val stops: List<Stop>
): Parcelable