package com.vs.schoolmessenger.Parent.BusTracking.Model.BusList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusListData(
    val student_id: String?,
    val student_name: String?,
    val admission_no: String?,
    val route_id: String?,
    val route_name: String?,
    val stop_id: String?,
    val stop_name: String?,
    val vehicle_id: String?,
    val vehicle_no: String?,
    val tentative_pickup_time: String?,
    val tentative_drop_time: String?,
    val stopping_points: List<StoppingPoint>
) : Parcelable