package com.vs.schoolmessenger.FCM

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class AcceptReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        try {

            Log.d(
                "ACCEPT_RECEIVER",
                "ACCEPT CLICKED"
            )

            val notificationId =
                intent.getIntExtra(
                    "NOTIFICATION_ID",
                    1001
                )

            val data =
                intent.getSerializableExtra("DATA")
                        as? HashMap<String, String>

            // =========================
            // MARK HANDLED
            // =========================

            CallStateManager.markHandled(
                context,
                notificationId
            )

            // =========================
            // STOP RINGTONE
            // =========================

            RingtoneHelper.stop()

            // =========================
            // CLOSE SMALL NOTIFICATION
            // =========================

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.cancel(notificationId)

            // =========================
            // STOP FOREGROUND SERVICE
            // =========================

            context.stopService(
                Intent(
                    context,
                    CallForegroundService::class.java
                )
            )

            // =========================
            // OPEN ACTIVITY
            // =========================

            val activityIntent =
                Intent(
                    context,
                    IncomingCallActivity::class.java
                )

            activityIntent.putExtra(
                "DATA",
                data
            )

            activityIntent.putExtra(
                "NOTIFICATION_ID",
                notificationId
            )

            activityIntent.putExtra(
                "AUTO_ACCEPT",
                true
            )

            activityIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )

            // ANDROID 12+ FIX
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                context.startActivity(activityIntent)

            } else {

                context.startActivity(activityIntent)
            }

            Log.d(
                "ACCEPT_RECEIVER",
                "ACTIVITY STARTED"
            )

        } catch (e: Exception) {

            Log.e(
                "ACCEPT_RECEIVER",
                "ERROR = ${e.message}"
            )
        }
    }
}