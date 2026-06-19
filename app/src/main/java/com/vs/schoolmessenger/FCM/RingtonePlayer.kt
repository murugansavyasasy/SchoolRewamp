package com.vs.schoolmessenger.FCM

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

object RingtonePlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun start(context: Context) {

        try {

            stop()

            if (isPhoneBusy(context)) {

                Log.d(
                    "RINGTONE",
                    "User is already on a phone call. Skip ringtone and vibration."
                )

                return
            }

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                                as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

            when (audioManager.ringerMode) {

                AudioManager.RINGER_MODE_SILENT -> {

                    Log.d(
                        "RINGTONE",
                        "Phone is SILENT. No ringtone and no vibration."
                    )

                    return
                }

                AudioManager.RINGER_MODE_VIBRATE -> {

                    Log.d(
                        "RINGTONE",
                        "Phone is VIBRATE. Vibrating only."
                    )

                    startVibration()

                    return
                }

                AudioManager.RINGER_MODE_NORMAL -> {

                    Log.d(
                        "RINGTONE",
                        "Phone is NORMAL. Playing ringtone + vibration."
                    )

                    startVibration()

                    val uri = Uri.parse(
                        "android.resource://${context.packageName}/${R.raw.call_notification}"
                    )

                    mediaPlayer = MediaPlayer().apply {

                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )

                        setDataSource(context, uri)

                        isLooping = true

                        setOnPreparedListener {
                            it.start()
                            Log.d("RINGTONE", "Ringtone Started")
                        }

                        setOnErrorListener { _, what, extra ->
                            Log.e(
                                "RINGTONE",
                                "Error what=$what extra=$extra"
                            )
                            true
                        }

                        prepareAsync()
                    }
                }
            }

        } catch (e: Exception) {

            Log.e(
                "RINGTONE",
                "START ERROR",
                e
            )
        }
    }

    private fun startVibration() {

        try {

            vibrator?.let {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    val effect =
                        VibrationEffect.createWaveform(
                            longArrayOf(
                                0,
                                1000,
                                1000
                            ),
                            0
                        )

                    it.vibrate(effect)

                } else {

                    @Suppress("DEPRECATION")
                    it.vibrate(
                        longArrayOf(
                            0,
                            1000,
                            1000
                        ),
                        0
                    )
                }
            }

        } catch (e: Exception) {

            Log.e(
                "RINGTONE",
                "VIBRATION ERROR",
                e
            )
        }
    }

    fun stop() {

        try {

            mediaPlayer?.apply {

                if (isPlaying) {
                    stop()
                }

                reset()
                release()
            }

            vibrator?.cancel()

        } catch (e: Exception) {

            Log.e(
                "RINGTONE",
                "STOP ERROR",
                e
            )

        } finally {

            mediaPlayer = null
            vibrator = null
        }
    }

    fun isPlaying(): Boolean {

        return mediaPlayer?.isPlaying ?: false
    }


    private fun isPhoneBusy(context: Context): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("RingtonePlayer", "READ_PHONE_STATE permission not granted")
                false
            } else {
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                telephonyManager.callState != TelephonyManager.CALL_STATE_IDLE
            }
        } catch (e: Exception) {
            Log.e("RingtonePlayer", "CALL STATE ERROR", e)
            false
        }
    }
}