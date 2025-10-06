package com.vs.schoolmessenger.FCM

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessaging"
        private const val CHANNEL_ID = "fcm_default_channel"
        private const val CHANNEL_NAME = "Custom Notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived called")

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Default Title"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Default Body"
        val imageUrl = remoteMessage.data["imageUrl"]
        val menuName = remoteMessage.data["menu_name"] ?: "Messages"
        val menuId = remoteMessage.data["menu_id"]?.toIntOrNull() ?: 1
        val msg_id = remoteMessage.data["msg_id"]?.toIntOrNull() ?: 1

        Log.d(TAG, "Data received: title=$title, body=$body, imageUrl=$imageUrl, menu=$menuName, id=$menuId, msg=$msg_id")

        sendNotification(title, body, imageUrl, menuName, menuId, msg_id)
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
    }

    private fun sendNotification(title: String?, messageBody: String?, imageUrl: String?, menuName: String, menuId: Int, msg_id: Int) {
        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Notification permission not granted")
                return
            }
        }

        // Create Intent for notification tap
        val intent = Intent(this, Splash::class.java).apply {
            putExtra(Constant.menu_name, menuName)
            putExtra(Constant.menu_id, menuId)
            putExtra(Constant.msg_id, msg_id)
            putExtra(Constant.fromNotification, true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification channel
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for custom notifications"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }

        // Try simple notification first to isolate RemoteViews issues
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.school_chimes)
            .setContentTitle(title ?: "Default Title")
            .setContentText(messageBody ?: "Default Body")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Handle custom notification with RemoteViews
        try {
            val remoteView = RemoteViews(packageName, R.layout.custom_notification).apply {
                setTextViewText(R.id.notification_title, title ?: "Default Title")
                setTextViewText(R.id.notification_body, messageBody ?: "Default Body")
            }

            // Handle image download
            var bitmap: Bitmap? = null
            if (!imageUrl.isNullOrEmpty()) {
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
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d(TAG, "Notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}")
        }
    }
}