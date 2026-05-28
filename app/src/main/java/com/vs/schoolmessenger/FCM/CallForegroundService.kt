package com.vs.schoolmessenger.FCM

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CallForegroundService : Service() {

    private val stopHandler =
        Handler(Looper.getMainLooper())

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        Log.d("CALL_SERVICE", "SERVICE STARTED")

        val data =
            intent?.getSerializableExtra("DATA")
                    as? HashMap<String, String>

        val notificationId =
            intent?.getIntExtra(
                "NOTIFICATION_ID",
                1001
            ) ?: 1001

        val notification =
            CallNotificationHelper.buildIncomingNotification(
                this,
                data ?: hashMapOf(),
                notificationId
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )

        } else {

            startForeground(
                notificationId,
                notification
            )
        }

        Log.d("CALL_SERVICE", "FOREGROUND STARTED")

        RingtoneHelper.start(this)

        stopHandler.postDelayed({

            Log.d(
                "CALL_SERVICE",
                "HANDLED = ${
                    CallStateManager.isHandled(
                        this,
                        notificationId
                    )
                }"
            )

            if (!CallStateManager.isHandled(this, notificationId)) {

                Log.d("CALL_SERVICE", "TIMEOUT")

                RingtoneHelper.stop()

                val closeIntent =
                    Intent(CallConstants.ACTION_CLOSE_CALL)

                LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcast(closeIntent)

                Log.d(
                    "CALL_SERVICE",
                    "CLOSE BROADCAST SENT"
                )

                Handler(Looper.getMainLooper()).postDelayed({

                    val manager =
                        getSystemService(
                            NOTIFICATION_SERVICE
                        ) as NotificationManager

                    manager.cancel(notificationId)

                    Log.d(
                        "CALL_SERVICE",
                        "SHOW MISSED"
                    )

                    CallNotificationHelper
                        .showMissedNotification(
                            this,
                            data ?: hashMapOf(),
                            notificationId
                        )

                    stopForeground(
                        STOP_FOREGROUND_REMOVE
                    )

                    stopSelf()

                }, 1000)
            }

        }, 60000)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(
            "CALL_SERVICE",
            "SERVICE DESTROY"
        )

        RingtoneHelper.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}