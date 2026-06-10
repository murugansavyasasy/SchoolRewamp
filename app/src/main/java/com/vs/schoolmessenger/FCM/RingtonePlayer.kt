package com.vs.schoolmessenger.FCM

import android.content.Context
import android.media.MediaPlayer
import com.vs.schoolmessenger.R

object RingtonePlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context) {

        if (mediaPlayer?.isPlaying == true) {
            return
        }

        mediaPlayer = MediaPlayer.create(
            context,
            R.raw.call_notification
        )

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    fun stop() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}