package com.vs.schoolmessenger.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.GPSStatusListener

class GPSStatusReceiver(private val listener: GPSStatusListener) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            listener.onGPSStatusChanged(isGPSenabled)
        }
    }
}