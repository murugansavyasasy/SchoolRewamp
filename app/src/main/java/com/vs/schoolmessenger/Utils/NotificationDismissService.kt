package com.vs.schoolmessenger.Utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class NotificationDismissService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && "NOTIFICATION_DISMISSED" == intent.getAction()) {
            Log.d("Notification", "Notification was dismissed")
            Constant.mediaPlayer.stop()
        }
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}