package com.vs.schoolmessenger.FCM

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.vs.schoolmessenger.R

object RingtonePlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context) {

        Log.e("RINGTONE", "START CALLED", Throwable())

        try {

            if (mediaPlayer?.isPlaying == true) {
                return
            }

            mediaPlayer = MediaPlayer.create(
                context.applicationContext,
                R.raw.call_notification
            )

            mediaPlayer?.isLooping = true
            mediaPlayer?.start()

            Log.d("RINGTONE", "STARTED")

        } catch (e: Exception) {
            Log.e("RINGTONE", "START ERROR", e)
        }
    }

    fun stop() {

        try {

            Log.d("RINGTONE", "STOPPING")

            mediaPlayer?.apply {

                if (isPlaying) {
                    stop()
                }

                release()
            }

        } catch (e: Exception) {
            Log.e("RINGTONE", "STOP ERROR", e)
        } finally {

            mediaPlayer = null

            Log.d("RINGTONE", "STOPPED")
        }
    }
}