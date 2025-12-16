package com.vs.schoolmessenger.Utils.CallTesting

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vs.schoolmessenger.R

class IncomingCallService : Service() {
    private val CHANNEL_ID = "voip_call_channel"
    private val NOTIF_ID = 1001
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val caller = intent?.getStringExtra("caller_name") ?: "Caller"
        val voiceUrl = intent?.getStringExtra("voice_url") ?: ""
        showIncomingCallNotification(caller, voiceUrl)
// We won't keep running endless; stop when user rejects or after
//        timeout (you can add logic)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            )
            chan.description = "Channel for incoming VoIP calls"
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(chan)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun showIncomingCallNotification(
        callerName: String, voiceUrl:
        String
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
// PendingIntent to open CallActivity when user taps Accept
        val acceptIntent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("voice_url", voiceUrl)
            putExtra("caller_name", callerName)
        }
        val acceptPending = PendingIntent.getActivity(
            this, 1, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
// PendingIntent for Reject action (BroadcastReceiver)
        val rejectIntent = Intent(this, RejectReceiver::class.java)
        val rejectPending = PendingIntent.getBroadcast(
            this, 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Incoming Voice Call")
            .setContentText(callerName)
            .setSmallIcon(R.drawable.phone_call_icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_CALL)
            .addAction(R.drawable.phone_call_icon, "Accept", acceptPending)
            .addAction(R.drawable.ic_call_answer, "Reject", rejectPending)
            .setAutoCancel(true)
            .setOngoing(true)
// Full-screen intent — opens activity in front (when allowed)
        builder.setFullScreenIntent(acceptPending, true)
        startForeground(NOTIF_ID, builder.build())
    }
}