package com.vs.schoolmessenger.Utils.CallTesting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class CallActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            MyFirebaseMessagingService.ACTION_ANSWER -> {
                NotificationManagerCompat.from(context)
                    .cancel(MyFirebaseMessagingService.NOTIF_ID)

                val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                    putExtra("voice_url", intent.getStringExtra("voice_url"))
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }

            MyFirebaseMessagingService.ACTION_REJECT -> {
                NotificationManagerCompat.from(context)
                    .cancel(MyFirebaseMessagingService.NOTIF_ID)
            }
        }
    }
}
