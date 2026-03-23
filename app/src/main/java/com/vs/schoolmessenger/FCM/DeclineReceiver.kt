package com.vs.schoolmessenger.FCM

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeclineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RingtoneHelper.stop()
        val id = intent.getIntExtra("NOTIFICATION_ID", 1001)
        CallStateManager.markHandled(context, id)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)
    }
}