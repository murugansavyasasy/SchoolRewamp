package com.vs.schoolmessenger.FCM

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.vs.schoolmessenger.Utils.Constant

class NotificationDismissService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action == Constant.NOTIFICATION_DISMISSED) {
                try {
                    if (Constant.mediaPlayer.isPlaying) {
                        Constant.mediaPlayer.stop()
                    }
                    Constant.mediaPlayer.reset()
                    Constant.mediaPlayer.release()
                } catch (ignored: Exception) {
                }
                // Recreate a fresh instance so code expecting non-null can use it
                try {
                    Constant.mediaPlayer = android.media.MediaPlayer()
                } catch (ignored: Exception) {
                }
            }
        } catch (ignored: Exception) {
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
