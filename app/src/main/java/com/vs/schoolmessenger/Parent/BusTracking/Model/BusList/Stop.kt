package com.vs.schoolmessenger.Parent.BusTracking.Model.BusList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stop (
    val stop_id: String,
    val stop_name: String,
    val stop_time: String,
    val latitude: String,
    val longitude: String,
    val landmark: String,
    val is_my_stop: Boolean
) : Parcelable