package com.vs.schoolmessenger.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("remoteMessage", remoteMessage.toString())

        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            val image = remoteMessage.data["image"]
            sendNotification(title, body, image)
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        println("FCM Token: $token")
    }

    private fun sendNotification(title: String?, messageBody: String?, imageUrl: String?) {
        val intent = Intent(this, Splash::class.java)
        intent.putExtra(Constant.menu_name, "Messages")
        intent.putExtra(Constant.menu_id, 1)
        intent.putExtra(Constant.msg_id, 1)
        intent.putExtra(Constant.fromNotification, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteView = RemoteViews(packageName, R.layout.custom_notification)
        remoteView.setTextViewText(R.id.notification_title, title)
        remoteView.setTextViewText(R.id.notification_body, messageBody)

        Thread {
            try {
                var bitmap: Bitmap? = null
                if (!imageUrl.isNullOrEmpty()) {
                    val url = URL(imageUrl)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(input)
                }

                if (bitmap != null) {
                    remoteView.setImageViewBitmap(R.id.notification_imageview, bitmap)
                    remoteView.setViewVisibility(R.id.notification_imageview, View.VISIBLE)
                } else {
                    remoteView.setViewVisibility(R.id.notification_imageview, View.GONE)
                }

                val channelId = "custom_channel"
                val builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.school_chimes)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteView)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Custom Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    manager.createNotificationChannel(channel)
                }

                manager.notify(0, builder.build())

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


}