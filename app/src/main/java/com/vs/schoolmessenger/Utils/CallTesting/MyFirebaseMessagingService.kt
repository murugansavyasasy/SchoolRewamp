package com.vs.schoolmessenger.Utils.CallTesting

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vs.schoolmessenger.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "incoming_call_channel"
        const val NOTIF_ID = 2001
        const val ACTION_ANSWER = "com.vs.schoolmessenger.ACTION_ANSWER"
        const val ACTION_REJECT = "com.vs.schoolmessenger.ACTION_REJECT"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        Log.e("FCM_DEBUG", "DATA RECEIVED: $data")

        if (data["type"] == "voip_call") {
            showIncomingCallNotification(
                data["caller_name"] ?: "School Caller",
                data["voice_url"] ?: ""
            )
        }
    }

    private fun showIncomingCallNotification(callerName: String, voiceUrl: String) {
        createChannelIfNeeded()

        // Full-screen intent
        val fsIntent = Intent(this, CallActivity::class.java).apply {
            putExtra("voice_url", voiceUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fsPendingIntent = PendingIntent.getActivity(
            this, 3001, fsIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Answer
        val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = ACTION_ANSWER
            putExtra("voice_url", voiceUrl)
        }

        val answerPending = PendingIntent.getBroadcast(
            this, 3002, answerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Reject
        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = ACTION_REJECT
        }

        val rejectPending = PendingIntent.getBroadcast(
            this, 3003, rejectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Caller information
        val caller = Person.Builder()
            .setName(callerName)
            .setImportant(true)
            .build()

        val callStyle = Notification.CallStyle.forIncomingCall(
            caller,
            rejectPending,
            answerPending
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.phone_call_icon)
            .setContentTitle("Incoming Call")
            .setContentText(callerName)
            .setCategory(Notification.CATEGORY_CALL)
            .setStyle(callStyle)
            .setOngoing(true)
            .setFullScreenIntent(fsPendingIntent, true)
            .build().apply {
                flags = flags or Notification.FLAG_INSISTENT
            }

        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return

            val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.call_notification}")

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.setSound(soundUri, attributes)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            nm.createNotificationChannel(channel)
        }
    }
}












//package com.vs.schoolmessenger.Utils.CallTesting
//
//import android.content.Intent
//import android.util.Log
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(message: RemoteMessage) {
//        val data = message.data
//
//        if (data["type"] == "voip_call") {
//Log.d("isComing","isComing")
//            val intent = Intent(this, IncomingActivity::class.java).apply {
//                addFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK or
//                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                            Intent.FLAG_ACTIVITY_SINGLE_TOP
//                )
//                putExtra("caller_name", data["caller_name"])
//                putExtra("voice_url", data["voice_url"])
//            }
//
//            startActivity(intent)
//        }
//    }
//}
//
//
//
//
//
////package com.vs.schoolmessenger.Utils.CallTesting
////
////import android.os.Bundle
////import android.telecom.TelecomManager
////import android.util.Log
////import com.google.firebase.messaging.FirebaseMessagingService
////import com.google.firebase.messaging.RemoteMessage
////
////class MyFirebaseMessagingService : FirebaseMessagingService() {
////
////    override fun onMessageReceived(message: RemoteMessage) {
////        val data = message.data
////        Log.d("FCM", "Received data: $data")
////
////        if (data["type"] == "voip_system_call") {
////            triggerSystemIncomingCall(
////                data["caller_name"] ?: "School Caller",
////                data["voice_url"] ?: ""
////            )
////        }
////    }
////
////    private fun triggerSystemIncomingCall(callerName: String, voiceUrl: String) {
////        val telecom = getSystemService(TELECOM_SERVICE) as TelecomManager
////        val handle = PhoneAccountHelper.getHandle(this)
////
////        val extras = Bundle().apply {
////            putString("caller_name", callerName)
////            putString("voice_url", voiceUrl)
////        }
////
////        telecom.addNewIncomingCall(handle, extras)
////    }
////}
////
////
////
////
////
////
////
////
//////package com.vs.schoolmessenger.Utils.CallTesting
//////
//////
//////import android.os.Bundle
//////import android.telecom.TelecomManager
//////import android.util.Log
//////import com.google.firebase.messaging.FirebaseMessagingService
//////import com.google.firebase.messaging.RemoteMessage
//////
//////class MyFirebaseMessagingService : FirebaseMessagingService() {
//////
//////    override fun onMessageReceived(message: RemoteMessage) {
//////        val data = message.data
//////        Log.d("FCM", "Received data: $data")
//////
//////        if (data["type"] == "voip_system_call") {
//////            triggerSystemIncomingCall(
//////                data["caller_name"] ?: "School Caller",
//////                data["voice_url"] ?: ""
//////            )
//////        }
//////    }
//////
//////    private fun triggerSystemIncomingCall(callerName: String, voiceUrl: String) {
//////        val telecom = getSystemService(TELECOM_SERVICE) as TelecomManager
//////        val handle = PhoneAccountHelper.getHandle(this)
//////
//////        val extras = Bundle().apply {
//////            putString("caller_name", callerName)
//////            putString("voice_url", voiceUrl)
//////        }
//////
//////        telecom.addNewIncomingCall(handle, extras)
//////    }
//////}
