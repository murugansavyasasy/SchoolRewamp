package com.vs.schoolmessenger.Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vs.schoolmessenger.School.MarkYourAttendance.LocationLatLongListener

class LocationHelper(
    private val context: Context,
    private val listener: LocationLatLongListener,
    private val type: String
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // intervalMillis
    )
        .setGranularity(Granularity.GRANULARITY_FINE)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(5000L)
        .setMaxUpdateDelayMillis(0L)
        .setMaxUpdates(1)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                location?.let {
                    listener.onLocationReturn(it.latitude, it.longitude, type)
                    Log.d("LocationHelper", "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
                }
            }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

    fun getFreshLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions not granted, handle this in your activity/fragment
            Log.e("LocationHelper", "Location permissions not granted")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}
