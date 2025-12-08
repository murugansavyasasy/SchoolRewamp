package com.vs.schoolmessenger.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vs.schoolmessenger.R
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class LocationService : Service() {

    private lateinit var socket: Socket
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        // Initialize socket
        socket = IO.socket("http://YOUR_SERVER_IP:3000")
        socket.connect()

        // Init location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val data = JSONObject().apply {
                    put("busId", "bus_101")
                    put("lat", location.latitude)
                    put("lng", location.longitude)
                }
                socket.emit("busLocation", data)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startForegroundService()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "bus_tracking_channel"
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Bus Tracking", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bus Tracking")
            .setContentText("Tracking location in background")
            .setSmallIcon(R.drawable.ic_account)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        socket.disconnect()
    }
}