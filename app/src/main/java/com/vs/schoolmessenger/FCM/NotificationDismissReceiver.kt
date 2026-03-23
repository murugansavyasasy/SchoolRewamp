package com.vs.schoolmessenger.FCM

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getIntExtra("NOTIFICATION_ID", 1001)
        val audioUrl = intent.getStringExtra("AUDIO_URL")

        Log.d("MISSED_DEBUG", "Notification dismissed")

        RingtoneHelper.stop()
        if (CallStateManager.isHandled(context, id)) {
            Log.d("MISSED_DEBUG", "Handled → skip missed")
            CallStateManager.clear(context, id)
            return
        }
    }
}