package com.vs.schoolmessenger.FCM

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.vs.schoolmessenger.R

object RingtoneHelper {

//    private var ringtone: Ringtone? = null
//    private var vibrator: Vibrator? = null

    var isPlaying = false

    fun start(context: Context) {

        if (isPlaying) {
            return
        }
        try {
            val uri =
                Uri.parse("android.resource://${context.packageName}/${R.raw.call_notification}")

//            ringtone = RingtoneManager.getRingtone(context, uri)
//            ringtone?.play()

//            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator?.vibrate(
//                    VibrationEffect.createWaveform(longArrayOf(0, 500, 1000), 0)
//                )
//            } else {
//                vibrator?.vibrate(longArrayOf(0, 500, 1000), 0)
//            }
            isPlaying = true

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
//        ringtone?.stop()
//        ringtone = null
//        vibrator?.cancel()
        isPlaying = false
    }
}