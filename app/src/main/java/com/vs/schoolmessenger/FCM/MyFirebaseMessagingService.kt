package com.vs.schoolmessenger.FCM

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vs.schoolmessenger.Auth.Base.MyApp
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived called")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_PAYLOAD", "FCM Payload: ${remoteMessage.data}")
            Log.d("FCM_PAYLOAD", "FCM remoteMessage: $remoteMessage")
        }
        // Example: Extract fields safely
        val title = remoteMessage.data[Constant.title_] ?: Constant.School_Chimes
        val body =
            remoteMessage.data[Constant.body_] ?: Constant.You_have_a_new_message_from_your_school
        val tone = remoteMessage.data[Constant.tone_] ?: Constant.normal
        val type = remoteMessage.data[Constant.type_] ?: Constant.normal
        val imageUrl = remoteMessage.data[Constant.imageurl] ?: Constant.Default
        val msgId =
            remoteMessage.data[Constant.msg_id] ?: ""  // Separate top-level msg_id from payload
        var msgInfo: String? = null
        Log.d("isNotificationType", remoteMessage.data[Constant.type_].toString())
        if (!type.equals(Constant.isCall)) {
            Log.d("msg_info", "msg_info")
            msgInfo = remoteMessage.data[Constant.msg_info] ?: ""
        }
        try {

            if (type == Constant.isCall) {

                val msgId =
                    remoteMessage.data["msg_id"]
                        ?: System.currentTimeMillis().toString()

                val notificationId = msgId.hashCode()

                CallStateManager.clear(
                    applicationContext,
                    notificationId
                )

                val keyguardManager =
                    getSystemService(Context.KEYGUARD_SERVICE)
                            as KeyguardManager

                val isLocked =
                    keyguardManager.isKeyguardLocked

                Log.d("FCM_LOG", "PHONE LOCKED = $isLocked")

                val serviceIntent =
                    Intent(this, CallForegroundService::class.java)

                serviceIntent.putExtra(
                    "DATA",
                    HashMap(remoteMessage.data)
                )

                serviceIntent.putExtra(
                    "NOTIFICATION_ID",
                    notificationId
                )

                serviceIntent.putExtra(
                    "IS_LOCKED",
                    isLocked
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    Log.d("FCM_LOG", "START FOREGROUND SERVICE")

                    startForegroundService(serviceIntent)

                } else {

                    startService(serviceIntent)
                }
            }
//            if (type == Constant.isCall) {
//
//
//                RingtoneHelper.start(applicationContext)
//                val notificationId = msgId.hashCode() ?: System.currentTimeMillis().toInt()
//
//                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//                val notification = CallNotificationHelper.buildIncomingNotification(
//                    context = this,
//                    data = HashMap(remoteMessage.data),
//                    notificationId = notificationId
//                )
//
//                manager.notify(notificationId, notification)
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (!CallStateManager.isHandled(applicationContext, notificationId)) {
//
//                        CallNotificationHelper.showMissedNotification(
//                            applicationContext,
//                            HashMap(remoteMessage.data),
//                            notificationId
//                        )
//                    }
//                }, 10000)
//            }
            else {

                val json = JSONObject(msgInfo)
                val menuId = json.optString(Constant.menu_id)
                val menuName = json.optString(Constant.menu_name)
                val receiver_type = json.optString(Constant.receiver_type)
                val receiver_id = json.optString(Constant.receiverid)
                val header_id = json.optString(Constant.header_id)
                val institute_id = json.optString(Constant.institute_id)
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

                Log.d(
                    "FCM_MSG_INFO",
                    "Parsed msg_info -> menu_id: $menuId, menu_name: $menuName, receiver_type: $receiver_type"
                )
            }

        } catch (e: Exception) {
            Log.e("FCM", "Error parsing msg_info: ${e.message}")
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
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
                MyApp.CHANNEL_ID,
                MyApp.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constant.Channel_for_custom_notifications
                enableLights(true)
                enableVibration(true)
                setSound(notificationSound, audioAttributes)
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
            setTextViewText(
                R.id.notification_body,
                messageBody ?: Constant.You_have_a_new_message_from_your_school
            )
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
            setTextViewText(
                R.id.notification_body,
                messageBody ?: Constant.You_have_a_new_message_from_your_school
            )
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
        val builder = NotificationCompat.Builder(this,  MyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.school_splash_logo)
            .setContentTitle(title ?: Constant.School_Chimes)
            .setContentText(
                messageBody ?: Constant.You_have_a_new_message_from_your_school
            ) // Fallback text
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
            val notificationId = uniqueID
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Expandable notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }
}





//package com.vs.schoolmessenger.FCM
//
//import android.app.KeyguardManager
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        Log.d("FCM_DATA", remoteMessage.data.toString())
//
//        val type = remoteMessage.data["type"] ?: ""
//
//        if (type == "isCall") {
//
//            val msgId =
//                remoteMessage.data["msg_id"]
//                    ?: System.currentTimeMillis().toString()
//
//            val notificationId = msgId.hashCode()
//
//            CallStateManager.clear(
//                applicationContext,
//                notificationId
//            )
//
//            val keyguardManager =
//                getSystemService(Context.KEYGUARD_SERVICE)
//                        as KeyguardManager
//
//            val isLocked =
//                keyguardManager.isKeyguardLocked
//
//            Log.d("FCM_LOG", "PHONE LOCKED = $isLocked")
//
//            val serviceIntent =
//                Intent(this, CallForegroundService::class.java)
//
//            serviceIntent.putExtra(
//                "DATA",
//                HashMap(remoteMessage.data)
//            )
//
//            serviceIntent.putExtra(
//                "NOTIFICATION_ID",
//                notificationId
//            )
//
//            serviceIntent.putExtra(
//                "IS_LOCKED",
//                isLocked
//            )
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                Log.d("FCM_LOG", "START FOREGROUND SERVICE")
//
//                startForegroundService(serviceIntent)
//
//            } else {
//
//                startService(serviceIntent)
//            }
//        }
//    }
//}
