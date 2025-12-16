package com.vs.schoolmessenger.Utils.CallTesting

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vs.schoolmessenger.R

class CallForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        startSilentForeground()
    }

    private fun startSilentForeground() {
        val channelId = "call_fg_service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Service",
                NotificationManager.IMPORTANCE_MIN
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.phone_call_icon)
            .setContentTitle("Connecting call…")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(9999, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val url = intent?.getStringExtra("voice_url") ?: ""

        val callIntent = Intent(this, CallActivity::class.java).apply {
            putExtra("voice_url", url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(callIntent)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null
}
