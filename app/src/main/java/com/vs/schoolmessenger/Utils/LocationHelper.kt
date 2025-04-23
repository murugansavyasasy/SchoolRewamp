package com.vs.schoolmessenger.Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.Granularity
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
import com.vs.schoolmessenger.School.MarkYourAttendance.LocationLatLongListener

class LocationHelper(
    private val context: Context,
    private val listener: LocationLatLongListener,
    private val type: String
) {

//    private val fusedLocationClient: FusedLocationProviderClient =
//        LocationServices.getFusedLocationProviderClient(context)
//
//    private val locationRequest: LocationRequest = LocationRequest.Builder(
//        Priority.PRIORITY_HIGH_ACCURACY, 5000
//    )
//        .setGranularity(Granularity.GRANULARITY_FINE)
//        .setWaitForAccurateLocation(true)
//        .setMinUpdateIntervalMillis(5000)
//        .setMaxUpdateDelayMillis(0)
//        .setMaxUpdates(1)
//        .build()

//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            for (location in locationResult.locations) {
//                location?.let {
//                    listener.onLocationReturn(it.latitude, it.longitude, type)
//                    Log.d("Location", "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
//                }
//            }
//            // Stop location updates after first result
//            fusedLocationClient.removeLocationUpdates(this)
//        }
//    }

    fun getFreshLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permissions are not granted — handle externally
//            return
//        }
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
    }
}