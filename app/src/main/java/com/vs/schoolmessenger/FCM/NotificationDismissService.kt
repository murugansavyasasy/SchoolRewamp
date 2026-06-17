package com.vs.schoolmessenger.FCM

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class NotificationDismissService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && "NOTIFICATION_DISMISSED" == intent.action) {
            Log.d("Notification", "Notification was dismissed")
            RingtonePlayer.stop()
        }
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}