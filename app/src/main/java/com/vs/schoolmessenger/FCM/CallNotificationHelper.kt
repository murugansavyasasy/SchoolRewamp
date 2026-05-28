package com.vs.schoolmessenger.FCM

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.vs.schoolmessenger.R

object CallNotificationHelper {

    private const val CHANNEL_ID =
        "VOICE_ALERT"

    fun buildIncomingNotification(
        context: Context,
        data: HashMap<String, String>,
        notificationId: Int
    ): Notification {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // =========================
        // CHANNEL
        // =========================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Voice Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )

            channel.description =
                "School Voice Alert"

            channel.enableVibration(true)

            channel.setBypassDnd(true)

            channel.lockscreenVisibility =
                Notification.VISIBILITY_PUBLIC

            manager.createNotificationChannel(channel)
        }

        val remoteViews =
            RemoteViews(
                context.packageName,
                R.layout.notification_call
            )

        // =========================
        // OPEN ACTIVITY
        // =========================

        val openIntent =
            Intent(
                context,
                IncomingCallActivity::class.java
            )

        openIntent.putExtra(
            "DATA",
            data
        )

        openIntent.putExtra(
            "NOTIFICATION_ID",
            notificationId
        )

        openIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        // =========================
        // ACCEPT BUTTON
        // DIRECT ACTIVITY
        // =========================

        val acceptIntent =
            Intent(
                context,
                IncomingCallActivity::class.java
            )

        acceptIntent.putExtra(
            "DATA",
            data
        )

        acceptIntent.putExtra(
            "NOTIFICATION_ID",
            notificationId
        )

        acceptIntent.putExtra(
            "AUTO_ACCEPT",
            true
        )

        acceptIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        val acceptPendingIntent =
            PendingIntent.getActivity(
                context,
                notificationId + 2,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        remoteViews.setOnClickPendingIntent(
            R.id.btnAccept,
            acceptPendingIntent
        )

        // =========================
        // DECLINE BUTTON
        // =========================

        val declineIntent =
            Intent(
                context,
                DeclineReceiver::class.java
            )

        declineIntent.putExtra(
            "NOTIFICATION_ID",
            notificationId
        )

        declineIntent.putExtra(
            "DATA",
            data
        )

        val declinePendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        remoteViews.setOnClickPendingIntent(
            R.id.btnDecline,
            declinePendingIntent
        )

        // =========================
        // DISMISS
        // =========================

        val dismissIntent =
            Intent(
                context,
                NotificationDismissReceiver::class.java
            )

        dismissIntent.putExtra(
            "NOTIFICATION_ID",
            notificationId
        )

        dismissIntent.putExtra(
            "DATA",
            data
        )

        val deletePendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationId + 500,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        // =========================
        // NOTIFICATION
        // =========================

        return NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.sym_call_incoming
            )
            .setPriority(
                NotificationCompat.PRIORITY_MAX
            )
            .setCategory(
                NotificationCompat.CATEGORY_CALL
            )
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(
                pendingIntent,
                true
            )
            .setDeleteIntent(deletePendingIntent)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setStyle(
                NotificationCompat
                    .DecoratedCustomViewStyle()
            )
            .build()
    }

    fun showMissedNotification(
        context: Context,
        data: HashMap<String, String>,
        id: Int
    ) {

        val intent =
            Intent(
                context,
                IncomingCallActivity::class.java
            )

        intent.putExtra(
            "DATA",
            data
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                id + 9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.sym_call_missed
                )
                .setContentTitle(
                    "Missed Announcement"
                )
                .setContentText(
                    data["call_title"]
                        ?: "Tap to listen"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            id + 9999,
            notification
        )
    }
}





//package com.vs.schoolmessenger.FCM
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.widget.RemoteViews
//import androidx.core.app.NotificationCompat
//import com.vs.schoolmessenger.R
//
//object CallNotificationHelper {
//
//    private const val CHANNEL_ID =
//        "VOICE_ALERT"
//
//    fun buildIncomingNotification(
//        context: Context,
//        data: HashMap<String, String>,
//        notificationId: Int
//    ): Notification {
//
//        val manager =
//            context.getSystemService(
//                Context.NOTIFICATION_SERVICE
//            ) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            val channel =
//                NotificationChannel(
//                    CHANNEL_ID,
//                    "Voice Alerts",
//                    NotificationManager.IMPORTANCE_HIGH
//                )
//
//            channel.description =
//                "School Voice Alert"
//
//            channel.enableVibration(true)
//
//            channel.setBypassDnd(true)
//
//            channel.lockscreenVisibility =
//                Notification.VISIBILITY_PUBLIC
//
//            manager.createNotificationChannel(channel)
//        }
//
//        val remoteViews =
//            RemoteViews(
//                context.packageName,
//                R.layout.notification_call
//            )
//
//        // =========================
//        // OPEN ACTIVITY
//        // =========================
//
//        val openIntent =
//            Intent(
//                context,
//                IncomingCallActivity::class.java
//            )
//
//        openIntent.putExtra(
//            "DATA",
//            data
//        )
//
//        openIntent.putExtra(
//            "NOTIFICATION_ID",
//            notificationId
//        )
//
//        openIntent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP
//
//        val pendingIntent =
//            PendingIntent.getActivity(
//                context,
//                notificationId,
//                openIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or
//                        PendingIntent.FLAG_IMMUTABLE
//            )
//
//
//        // =========================
//// ACCEPT
//// =========================
//
//        val acceptIntent =
//            Intent(
//                context,
//                AcceptReceiver::class.java
//            )
//
//        acceptIntent.putExtra(
//            "NOTIFICATION_ID",
//            notificationId
//        )
//
//        acceptIntent.putExtra(
//            "DATA",
//            data
//        )
//
//        val acceptPendingIntent =
//            PendingIntent.getBroadcast(
//                context,
//                notificationId + 2,
//                acceptIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or
//                        PendingIntent.FLAG_IMMUTABLE
//            )
//
//        remoteViews.setOnClickPendingIntent(
//            R.id.btnAccept,
//            acceptPendingIntent
//        )
//
//        // =========================
//        // DECLINE
//        // =========================
//
//        val declineIntent =
//            Intent(
//                context,
//                DeclineReceiver::class.java
//            )
//
//        declineIntent.putExtra(
//            "NOTIFICATION_ID",
//            notificationId
//        )
//
//        declineIntent.putExtra(
//            "DATA",
//            data
//        )
//
//        val declinePendingIntent =
//            PendingIntent.getBroadcast(
//                context,
//                notificationId + 1,
//                declineIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or
//                        PendingIntent.FLAG_IMMUTABLE
//            )
//
//        // =========================
//        // DISMISS
//        // =========================
//
//        val dismissIntent =
//            Intent(
//                context,
//                NotificationDismissReceiver::class.java
//            )
//
//        dismissIntent.putExtra(
//            "NOTIFICATION_ID",
//            notificationId
//        )
//
//        dismissIntent.putExtra(
//            "DATA",
//            data
//        )
//
//        val deletePendingIntent =
//            PendingIntent.getBroadcast(
//                context,
//                notificationId + 500,
//                dismissIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or
//                        PendingIntent.FLAG_IMMUTABLE
//            )
//
//        // =========================
//        // BUTTONS
//        // =========================
//
//        remoteViews.setOnClickPendingIntent(
//            R.id.btnDecline,
//            declinePendingIntent
//        )
//
//        // =========================
//        // NOTIFICATION
//        // =========================
//
//        return NotificationCompat.Builder(
//            context,
//            CHANNEL_ID
//        )
//            .setSmallIcon(
//                android.R.drawable.sym_call_incoming
//            )
//            .setPriority(
//                NotificationCompat.PRIORITY_MAX
//            )
//            .setCategory(
//                NotificationCompat.CATEGORY_CALL
//            )
//            .setVisibility(
//                NotificationCompat.VISIBILITY_PUBLIC
//            )
//            .setOngoing(false)
//            .setAutoCancel(false)
//            .setFullScreenIntent(
//                pendingIntent,
//                true
//            )
//            .setDeleteIntent(deletePendingIntent)
//            .setContentIntent(pendingIntent)
//            .setCustomContentView(remoteViews)
//            .setCustomBigContentView(remoteViews)
//            .setStyle(
//                NotificationCompat
//                    .DecoratedCustomViewStyle()
//            )
//            .build()
//    }
//
//    fun showMissedNotification(
//        context: Context,
//        data: HashMap<String, String>,
//        id: Int
//    ) {
//
//        val intent =
//            Intent(
//                context,
//                IncomingCallActivity::class.java
//            )
//
//        intent.putExtra(
//            "DATA",
//            data
//        )
//
//        val pendingIntent =
//            PendingIntent.getActivity(
//                context,
//                id + 9999,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or
//                        PendingIntent.FLAG_IMMUTABLE
//            )
//
//        val notification =
//            NotificationCompat.Builder(
//                context,
//                CHANNEL_ID
//            )
//                .setSmallIcon(
//                    android.R.drawable.sym_call_missed
//                )
//                .setContentTitle(
//                    "Missed Announcement"
//                )
//                .setContentText(
//                    data["call_title"]
//                        ?: "Tap to listen"
//                )
//                .setPriority(
//                    NotificationCompat.PRIORITY_HIGH
//                )
//                .setAutoCancel(true)
//                .setContentIntent(
//                    pendingIntent
//                )
//                .build()
//
//        val manager =
//            context.getSystemService(
//                Context.NOTIFICATION_SERVICE
//            ) as NotificationManager
//
//        manager.notify(
//            id + 9999,
//            notification
//        )
//    }
//}
//
//
//
//
//
//
//
////package com.vs.schoolmessenger.FCM
////
////import android.app.Notification
////import android.app.NotificationChannel
////import android.app.NotificationManager
////import android.app.PendingIntent
////import android.content.Context
////import android.content.Intent
////import android.os.Build
////import android.widget.RemoteViews
////import androidx.core.app.NotificationCompat
////import com.vs.schoolmessenger.R
////
////object CallNotificationHelper {
////
////    private const val CHANNEL_ID = "VOICE_ALERT"
////
////    fun buildIncomingNotification(
////        context: Context,
////        data: HashMap<String, String>,
////        notificationId: Int
////    ): Notification {
////
////        val manager =
////            context.getSystemService(
////                Context.NOTIFICATION_SERVICE
////            ) as NotificationManager
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////
////            val channel =
////                NotificationChannel(
////                    CHANNEL_ID,
////                    "Voice Alerts",
////                    NotificationManager.IMPORTANCE_HIGH
////                )
////
////            channel.description =
////                "School Voice Alert"
////
////            channel.enableVibration(true)
////
////            channel.setBypassDnd(true)
////
////            channel.lockscreenVisibility =
////                Notification.VISIBILITY_PUBLIC
////
////            manager.createNotificationChannel(channel)
////        }
////
////        val remoteViews =
////            RemoteViews(
////                context.packageName,
////                R.layout.notification_call
////            )
////
////        val openIntent =
////            Intent(
////                context,
////                IncomingCallActivity::class.java
////            )
////
////        openIntent.putExtra("DATA", data)
////
////        openIntent.putExtra(
////            "NOTIFICATION_ID",
////            notificationId
////        )
////
////        openIntent.flags =
////            Intent.FLAG_ACTIVITY_NEW_TASK or
////                    Intent.FLAG_ACTIVITY_CLEAR_TOP
////
////        val pendingIntent =
////            PendingIntent.getActivity(
////                context,
////                notificationId,
////                openIntent,
////                PendingIntent.FLAG_UPDATE_CURRENT or
////                        PendingIntent.FLAG_IMMUTABLE
////            )
////
////        val declineIntent =
////            Intent(
////                context,
////                DeclineReceiver::class.java
////            )
////
////        declineIntent.putExtra(
////            "NOTIFICATION_ID",
////            notificationId
////        )
////
////        val declinePendingIntent =
////            PendingIntent.getBroadcast(
////                context,
////                notificationId + 1,
////                declineIntent,
////                PendingIntent.FLAG_UPDATE_CURRENT or
////                        PendingIntent.FLAG_IMMUTABLE
////            )
////
////        remoteViews.setOnClickPendingIntent(
////            R.id.btnDecline,
////            declinePendingIntent
////        )
////
////        return NotificationCompat.Builder(
////            context,
////            CHANNEL_ID
////        )
////            .setSmallIcon(android.R.drawable.sym_call_incoming)
////            .setPriority(NotificationCompat.PRIORITY_MAX)
////            .setCategory(NotificationCompat.CATEGORY_CALL)
////            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
////            .setOngoing(true)
////            .setAutoCancel(false)
////            .setFullScreenIntent(
////                pendingIntent,
////                true
////            )
////            .setContentIntent(pendingIntent)
////            .setCustomContentView(remoteViews)
////            .setCustomBigContentView(remoteViews)
////            .setStyle(
////                NotificationCompat.DecoratedCustomViewStyle()
////            )
////            .build()
////    }
////
////    fun showMissedNotification(
////        context: Context,
////        data: HashMap<String, String>,
////        id: Int
////    ) {
////
////        val intent =
////            Intent(
////                context,
////                IncomingCallActivity::class.java
////            )
////
////        intent.putExtra("DATA", data)
////
////        val pendingIntent =
////            PendingIntent.getActivity(
////                context,
////                id,
////                intent,
////                PendingIntent.FLAG_UPDATE_CURRENT or
////                        PendingIntent.FLAG_IMMUTABLE
////            )
////
////        val notification =
////            NotificationCompat.Builder(
////                context,
////                CHANNEL_ID
////            )
////                .setSmallIcon(
////                    android.R.drawable.sym_call_missed
////                )
////                .setContentTitle(
////                    "Missed Announcement"
////                )
////                .setContentText(
////                    data["call_title"]
////                        ?: "Tap to listen"
////                )
////                .setPriority(
////                    NotificationCompat.PRIORITY_HIGH
////                )
////                .setContentIntent(pendingIntent)
////                .setAutoCancel(true)
////                .build()
////
////        val manager =
////            context.getSystemService(
////                Context.NOTIFICATION_SERVICE
////            ) as NotificationManager
////
////        manager.notify(id + 9999, notification)
////    }
////}
////
////
////
//////package com.vs.schoolmessenger.FCM
//////
//////import android.app.Notification
//////import android.app.NotificationManager
//////import android.app.PendingIntent
//////import android.content.Context
//////import android.content.Intent
//////import android.widget.RemoteViews
//////import androidx.core.app.NotificationCompat
//////import com.vs.schoolmessenger.Auth.Base.MyApp
//////import com.vs.schoolmessenger.R
//////
//////object CallNotificationHelper {
//////
//////    fun buildIncomingNotification(
//////        context: Context,
//////        data: HashMap<String, String>,
//////        notificationId: Int
//////    ): Notification {
//////
//////        val channelId = "CALL_CHANNEL"
//////
//////        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//////
//////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//////            val channel = android.app.NotificationChannel(
//////                channelId,
//////                "Incoming Calls",
//////                NotificationManager.IMPORTANCE_HIGH
//////            ).apply {
//////                description = "Call Notifications"
//////                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//////                setBypassDnd(true)
//////            }
//////            manager.createNotificationChannel(channel)
//////        }
//////
//////        val remoteViews = RemoteViews(context.packageName, R.layout.notification_call)
//////
//////        val openIntent = Intent(context, IncomingCallActivity::class.java).apply {
//////            putExtra("DATA", data)
//////            putExtra("NOTIFICATION_ID", notificationId)
//////            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//////        }
//////
//////        val contentPendingIntent = PendingIntent.getActivity(
//////            context,
//////            notificationId,
//////            openIntent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        val fullScreenPendingIntent = PendingIntent.getActivity(
//////            context,
//////            notificationId + 100,
//////            openIntent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        val acceptIntent = Intent(context, IncomingCallActivity::class.java).apply {
//////            putExtra("DATA", data)
//////            putExtra("AUTO_PLAY", true)
//////            putExtra("NOTIFICATION_ID", notificationId)
//////            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//////        }
//////
//////        val acceptPendingIntent = PendingIntent.getActivity(
//////            context,
//////            notificationId + 1,
//////            acceptIntent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        val declineIntent = Intent(context, DeclineReceiver::class.java).apply {
//////            putExtra("DATA", data)
//////            putExtra("NOTIFICATION_ID", notificationId)
//////        }
//////
//////        val declinePendingIntent = PendingIntent.getBroadcast(
//////            context,
//////            notificationId + 2,
//////            declineIntent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
//////            putExtra("DATA", data)
//////            putExtra("NOTIFICATION_ID", notificationId)
//////            putExtra("IS_TIMEOUT", true)
//////        }
//////
//////        val deletePendingIntent = PendingIntent.getBroadcast(
//////            context,
//////            notificationId + 3,
//////            deleteIntent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        remoteViews.setOnClickPendingIntent(R.id.btnAccept, acceptPendingIntent)
//////        remoteViews.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)
//////
//////        return NotificationCompat.Builder(context, channelId)
//////            .setSmallIcon(android.R.drawable.sym_call_incoming)
//////            .setCustomContentView(remoteViews)
//////            .setCustomBigContentView(remoteViews)
//////            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//////            .setPriority(NotificationCompat.PRIORITY_MAX)
//////            .setCategory(NotificationCompat.CATEGORY_CALL)
//////            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//////            .setFullScreenIntent(fullScreenPendingIntent, true)
//////            .setContentIntent(contentPendingIntent)
//////            .setDeleteIntent(deletePendingIntent)
//////            .setOngoing(true)
//////            .setAutoCancel(false)
//////
//////            .build()
//////    }
//////
//////    fun showMissedNotification(context: Context, data: HashMap<String, String>, id: Int) {
//////        RingtoneHelper.stop()
//////
//////        val intent = Intent(context, IncomingCallActivity::class.java).apply {
//////            putExtra("DATA", data)
//////            putExtra("AUTO_PLAY", true)
//////        }
//////
//////        val pendingIntent = PendingIntent.getActivity(
//////            context,
//////            id,
//////            intent,
//////            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//////        )
//////
//////        val notification = NotificationCompat.Builder(context, MyApp.CHANNEL_ID)
//////            .setSmallIcon(android.R.drawable.sym_call_missed)
//////            .setContentTitle("Missed Announcement")
//////            .setContentText(data["call_title"] ?: "Tap to listen")
//////            .setPriority(NotificationCompat.PRIORITY_HIGH)
//////            .setContentIntent(pendingIntent)
//////            .setAutoCancel(true)
//////            .build()
//////
//////        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//////        manager.notify(id, notification)
//////    }
//////}