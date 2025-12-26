package com.vs.schoolmessenger.FCM

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.NotificationDismissService
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessaging"
        private const val CHANNEL_ID = "fcm_default_channel"
        private const val CHANNEL_NAME = "Custom Notifications"
    }


    private val handler = Handler()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "onMessageReceived called")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_PAYLOAD", "FCM Payload: ${remoteMessage.data}")
            Log.d("FCM_PAYLOAD", "FCM remoteMessage: ${remoteMessage.toString()}")
        }
        // Example: Extract fields safely
        val title = remoteMessage.data[Constant.title_] ?: Constant.School_Chimes
        val body =
            remoteMessage.data[Constant.body_] ?: Constant.You_have_a_new_message_from_your_school
        val tone = remoteMessage.data[Constant.tone_] ?: Constant.normal
        val type = remoteMessage.data[Constant.type_] ?: Constant.normal
        val isVoiceUrl = remoteMessage.data[Constant.isVoiceUrlNotifi] ?: Constant.normal
        val isWelcomeUrl = remoteMessage.data[Constant.isWelcomeUrlNotifi] ?: Constant.normal
        val imageUrl = remoteMessage.data[Constant.imageurl] ?: Constant.Default
        val msgId = remoteMessage.data[Constant.msg_id] ?: ""  // Separate top-level msg_id from payload
        var msgInfo: String? = null
        if (!type.equals(Constant.isCall)) {
            msgInfo = remoteMessage.data[Constant.msg_info] ?: ""
        }

        val receiver_id = remoteMessage.data[Constant.receiverid] ?: ""
        val circular_id = remoteMessage.data[Constant.circular_id] ?: ""
        val retrycount = remoteMessage.data[Constant.retrycount] ?: ""
        val ei1 = remoteMessage.data[Constant.ei1] ?: ""
        val ei2 = remoteMessage.data[Constant.ei2] ?: ""
        val ei3 = remoteMessage.data[Constant.ei3] ?: ""
        val ei4 = remoteMessage.data[Constant.ei4] ?: ""
        val ei5 = remoteMessage.data[Constant.ei5] ?: ""
        val role = remoteMessage.data[Constant.role] ?: ""
        val member_name = remoteMessage.data[Constant.member_name] ?: ""
        val school_name = remoteMessage.data[Constant.school_name] ?: ""
        val call_title = remoteMessage.data[Constant.call_title] ?: ""

        // Optional: Parse nested msg_info JSON if it's in valid JSON format
        try {
            // Firebase may send it like: {"menu_id":"39", "menu_name":"Attachments", ...}
            val json = JSONObject(msgInfo)
            val menuId = json.optString(Constant.menu_id)
            val menuName = json.optString(Constant.menu_name)
            val receiver_type = json.optString(Constant.receiver_type)
            val receiver_id = json.optString(Constant.receiverid)
            val header_id = json.optString(Constant.header_id)
            val institute_id = json.optString(Constant.institute_id)



            if (type.equals(Constant.isCall)) {
                sendNotificationCall(
                    title,
                    body,
                    receiver_id.toString(),
                    header_id.toString(),
                    receiver_type.toString(),isWelcomeUrl,isVoiceUrl,ei1,ei2,ei3,ei4,ei5,school_name,member_name,call_title,role,circular_id,retrycount
                )
            } else {
                sendNotification(
                    title,
                    body,
                    tone,
                    imageUrl,
                    menuName,
                    menuId.toIntOrNull() ?: 0,
                    header_id,  // Pass as String
                    msgId.toIntOrNull() ?: 0,  // Pass top-level msg_id separately if needed
                    receiver_type,
                    receiver_id,
                    institute_id
                )
            }
            Log.d(
                "FCM_MSG_INFO",
                "Parsed msg_info -> menu_id: $menuId, menu_name: $menuName, receiver_type: $receiver_type"
            )
        } catch (e: Exception) {
            Log.e("FCM", "Error parsing msg_info: ${e.message}")
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
    }

    private fun sendNotificationCall(
        title: String,
        body: String,
        receiver_id: String,
        headerId: String,
        receiverType: String,
        isWelcomeUrl: String,
        isVoiceUrl: String,
        ei1: String,
        ei2: String,
        ei3: String,
        ei4: String,
        ei5: String,
        school_name: String,
        member_name: String,
        call_title: String,
        role: String,
        circular_id: String,
        retrycount: String
    ) {
        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Notification permission not granted")
                return
            }
        }

        // Create Intent for notification tap
        val intent = Intent(this, NotificationCallScreen::class.java).apply {
            putExtra(Constant.menu_name, title)
            putExtra(Constant.isNotificationId, "")
            putExtra(Constant.isReceiverId, receiver_id)
            putExtra(Constant.retrycount, retrycount)
            putExtra(Constant.circularId, circular_id)
            putExtra(Constant.ei1, ei1)
            putExtra(Constant.ei2, ei2)
            putExtra(Constant.ei3, ei3)
            putExtra(Constant.ei4, ei4)
            putExtra(Constant.ei5, ei5)
            putExtra(Constant.role, role)
            putExtra(Constant.menuId, "")
            putExtra(Constant.school_name, school_name)
            putExtra(Constant.member_name, member_name)
            putExtra(Constant.call_title, call_title)
            putExtra(Constant.isVoiceUrlNotifi, isVoiceUrl)
            putExtra(Constant.isWelcomeUrlNotifi, isWelcomeUrl)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val uniqueID = (receiver_id + headerId).hashCode()
        val requestCode = uniqueID.takeIf { it != 0 } ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification channel
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://${packageName}/raw/call_notification")
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constant.Channel_for_custom_notifications
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                // 🔊 Play sound manually if needed
                if (!Constant.mediaPlayer.isPlaying) {
                    val mediaPlayer = MediaPlayer.create(this@MyFirebaseMessagingService, soundUri)
                    mediaPlayer.isLooping = false
                    mediaPlayer.start()
                    Constant.mediaPlayer = mediaPlayer
                }
            }
            manager.createNotificationChannel(channel)
            handler.postDelayed(stopMediaPlayerRunnable, 30000)

            Log.d(TAG, "Notification channel created")
        }

        // Try simple notification first to isolate RemoteViews issues
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.school_splash_logo)
            .setContentTitle(title ?: Constant.School_Chimes)
            .setContentText(body ?: Constant.You_have_a_new_message_from_your_school)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(createDeleteIntent()) // Add delete intent for dismissal
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Handle custom notification with RemoteViews
        try {
            val remoteView = RemoteViews(packageName, R.layout.custom_call_notification).apply {
                setTextViewText(R.id.notification_title, title ?: "School Chimes")

            }
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteView)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up custom notification: ${e.message}")
        }

        try {
//            manager.notify(System.currentTimeMillis().toInt(), builder.build())
            val uniqueID = (receiver_id + headerId).hashCode()
            val notificationId = uniqueID ?: (0..999999).random()
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    private fun createDeleteIntent(): PendingIntent {
        val intent = Intent(this, NotificationDismissService::class.java)
        intent.action = Constant.NOTIFICATION_DISMISSED

        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun sendNotification(
        title: String?,
        messageBody: String?,
        tone: String?,
        imageUrl: String?,
        menu_name: String,
        menuId: Int,
        headerId: String, // Changed to String
        msgId: Int, // Top-level msg_id from payload
        receiverType: String,
        receiverId: String,
        instituteId: String
    ) {
        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Notification permission not granted")
                return
            }
        }
        // Create Intent for notification tap
        val intent = Intent(this, Splash::class.java).apply {
            putExtra(Constant.menu_name, menu_name)
            putExtra(Constant.menu_id, menuId)
            putExtra(Constant.msg_id, msgId)
            putExtra(Constant.header_id, headerId)
            putExtra(Constant.receiver_type, receiverType)
            putExtra(Constant.receiverid, receiverId)
            putExtra(Constant.institute_id, instituteId)
            putExtra(Constant.fromNotification, true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val uniqueID = (receiverId + headerId).hashCode()
        val requestCode = uniqueID.takeIf { it != 0 } ?: System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val message = Uri.parse("android.resource://${packageName}/raw/message")
        val emergency_message = Uri.parse("android.resource://${packageName}/raw/emergencyvoice")
        var notificationSound: Uri? = null
        if (tone.equals(Constant.normal)) {
            notificationSound = message
        } else if (tone.equals(Constant.emergency_voice)) {
            notificationSound = emergency_message
        }
        // Create notification channel
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constant.Channel_for_custom_notifications
                enableLights(true)
                enableVibration(true)
                setSound(notificationSound, audioAttributes) // ✅ Custom tone for this channel
            }
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
        // Download image bitmap early (shared for both views)
        var bitmap: Bitmap? = null
        if (!imageUrl.isNullOrEmpty() && imageUrl != Constant.Default) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                val input = connection.inputStream
                bitmap = BitmapFactory.decodeStream(input)
                input.close()
                connection.disconnect()
                Log.d(TAG, "Image downloaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download image: ${e.message}")
            }
        }

        // Create collapsed RemoteViews (1 line for body with ellipsis)
        val remoteViewCollapsed = RemoteViews(packageName, R.layout.custom_notification).apply {
            setTextViewText(R.id.notification_title, title ?: Constant.School_Chimes)
            setTextViewText(R.id.notification_body, messageBody ?: Constant.You_have_a_new_message_from_your_school)
            setInt(R.id.notification_body, "setMaxLines", 1) // Show only 1 line initially
            // Handle image for collapsed (will hide if no space)
            if (bitmap != null) {
                setImageViewBitmap(R.id.notification_imageview, bitmap)
                setViewVisibility(R.id.notification_imageview, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_imageview, View.GONE)
            }
        }

        // Create expanded RemoteViews (up to 5 lines for body)
        val remoteViewExpanded = RemoteViews(packageName, R.layout.custom_notification).apply {
            setTextViewText(R.id.notification_title, title ?: Constant.School_Chimes)
            setTextViewText(R.id.notification_body, messageBody ?: Constant.You_have_a_new_message_from_your_school)
            setInt(R.id.notification_body, "setMaxLines", 100) // Show full multi-line content
            // Handle image for expanded (always visible if present)
            if (bitmap != null) {
                setImageViewBitmap(R.id.notification_imageview, bitmap)
                setViewVisibility(R.id.notification_imageview, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_imageview, View.GONE)
            }
        }

        // Build notification with separate views for collapsed/expanded states
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.school_splash_logo)
            .setContentTitle(title ?: Constant.School_Chimes)
            .setContentText(messageBody ?: Constant.You_have_a_new_message_from_your_school) // Fallback text
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCustomContentView(remoteViewCollapsed) // Collapsed state
            .setCustomBigContentView(remoteViewExpanded) // Expanded state

        // Handle custom notification with RemoteViews
        try {
            val remoteView = RemoteViews(packageName, R.layout.custom_notification).apply {
                setTextViewText(R.id.notification_title, title ?: Constant.School_Chimes)
                setTextViewText(
                    R.id.notification_body,
                    messageBody ?: Constant.You_have_a_new_message_from_your_school
                )
            }

            // Handle image download
            var bitmap: Bitmap? = null
            if (!imageUrl.isNullOrEmpty() && imageUrl != Constant.Default) {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()
                    val input = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                    connection.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download image: ${e.message}")
                }
            }

            if (bitmap != null) {
                remoteView.setImageViewBitmap(R.id.notification_imageview, bitmap)
                remoteView.setViewVisibility(R.id.notification_imageview, View.VISIBLE)
                Log.d(TAG, "Image set in notification")
            } else {
                remoteView.setViewVisibility(R.id.notification_imageview, View.GONE)
                Log.d(TAG, "No image set in notification")
            }

            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteView)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up custom notification: ${e.message}")
        }

        try {
            val uniqueID = (receiverId + headerId).hashCode()
            val notificationId = uniqueID ?: (0..999999).random()
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Expandable notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }

    private val stopMediaPlayerRunnable = Runnable {
        if (Constant.mediaPlayer != null && Constant.mediaPlayer.isPlaying) {
            Constant.mediaPlayer.stop()
            Constant.mediaPlayer.release()
            Constant.mediaPlayer = MediaPlayer()
        }
    }
}