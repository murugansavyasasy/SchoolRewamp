package com.vs.schoolmessenger.FCM

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.vs.schoolmessenger.R

object CallNotificationHelper {

    private const val CHANNEL_ID = "fcm_call_channel"

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildIncomingNotification(
        context: Context,
        audioUrl: String?,
        voiceId: String?,
        notificationId: Int
    ): Notification {

        createChannel(context)

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_call)

        val openIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra("AUDIO_URL", audioUrl)
            putExtra("VOICE_ID", voiceId)
            putExtra("NOTIFICATION_ID", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra("AUDIO_URL", audioUrl)
            putExtra("VOICE_ID", voiceId)
            putExtra("AUTO_PLAY", true)
            putExtra("NOTIFICATION_ID", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, DeclineReceiver::class.java).apply {
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("AUDIO_URL", audioUrl)
        }

        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("AUDIO_URL", audioUrl)
            putExtra("IS_TIMEOUT", true)
        }

        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.btnAccept, acceptPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .setDeleteIntent(deletePendingIntent)
            .setTimeoutAfter(10000)
            .build()
    }

    fun showMissedNotification(context: Context, audioUrl: String?, id: Int) {
        RingtoneHelper.stop()
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra("AUDIO_URL", audioUrl)
            putExtra("VOICE_ID", id.toString())
            putExtra("AUTO_PLAY", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_missed)
            .setContentTitle("Missed Announcement")
            .setContentText("Tap to listen")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}