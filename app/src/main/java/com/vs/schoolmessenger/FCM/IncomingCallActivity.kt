package com.vs.schoolmessenger.FCM

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ActivityIncomingCallBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallBinding

    private var player: ExoPlayer? = null

    private lateinit var audioManager: AudioManager

    private var notificationId = 1001

    private var timerJob: Job? = null

    private var isSpeakerEnabled = false

    private var welcomeUrl: String? = null

    private var mainUrl: String? = null

    private var totalDuration = 0L

    private var isTotalDurationListened = 0L

    private var welcomeDuration = 0L

    private var downX = 0f

    private var authViewModel: Auth? = null

    private var isIncomingCallData: HashMap<String, String>? = null

    private var isUserResponse: String? = "NO"

    private var isStartTime: String? = null

    private var isEndTime: String? = null

    private var isPaused = false

    private val closeReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                try {

                    RingtoneHelper.stop()

                    player?.stop()

                    player?.release()

                    player = null

                    finish()

                    finishAffinity()

                    finishAndRemoveTask()

                } catch (e: Exception) {

                    Log.e(
                        "CALL_ACTIVITY",
                        "CLOSE ERROR = ${e.message}"
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityIncomingCallBinding.inflate(layoutInflater)

        setContentView(binding.root)

        wakeScreen()

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                closeReceiver,
                IntentFilter(
                    CallConstants.ACTION_CLOSE_CALL
                )
            )

        audioManager =
            getSystemService(AUDIO_SERVICE) as AudioManager

        authViewModel =
            ViewModelProvider(this)[Auth::class.java]

        authViewModel!!.init()

        isIncomingCallData =
            intent.getSerializableExtra("DATA")
                    as? HashMap<String, String>

        welcomeUrl =
            isIncomingCallData?.get("welcome")

        mainUrl =
            isIncomingCallData?.get("url")

        notificationId =
            intent.getIntExtra(
                "NOTIFICATION_ID",
                1001
            )

        val manager =
            getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.cancel(notificationId)

        setupClicks()

        val autoAccept =
            intent.getBooleanExtra(
                "AUTO_ACCEPT",
                false
            )

        if (autoAccept) {

            acceptCall()
        }

        handleBusyState()
    }

    private fun wakeScreen() {

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O_MR1
        ) {

            setShowWhenLocked(true)

            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val keyguardManager =
            getSystemService(
                KEYGUARD_SERVICE
            ) as KeyguardManager

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            keyguardManager.requestDismissKeyguard(
                this,
                null
            )
        }
    }

    private fun isUserBusy(): Boolean {

        return audioManager.mode ==
                AudioManager.MODE_IN_CALL ||
                audioManager.mode ==
                AudioManager.MODE_IN_COMMUNICATION
    }

    private fun handleBusyState() {

        if (isUserBusy()) {

            RingtoneHelper.stop()

            binding.btnAnswer.alpha = 0.5f

            binding.btnAnswer.isEnabled = false

            binding.btnReject.isEnabled = true

            Toast.makeText(
                this,
                "User is busy now",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClicks() {

        binding.btnAnswer.setOnTouchListener(answerSwipe)

        binding.btnReject.setOnTouchListener(rejectSwipe)

        // END CALL
        binding.btnEndCall.setOnClickListener {

            isEndTime = getAcceptTime()

            isUserResponse = "DISCONNECTED"

            stopEverything()

            isUpdateNotificationCall()

            finishAndRemoveTask()
        }

        // SPEAKER
        binding.btnSpeaker.setOnClickListener {

            toggleSpeaker()
        }

        // PLAY PAUSE
        binding.btnPlayPause.setOnClickListener {

            togglePlayPause()
        }

        // FORWARD
        binding.btnForward.setOnClickListener {

            seekForward()
        }

        // BACKWARD
        binding.btnBackward.setOnClickListener {

            seekBackward()
        }
    }

    private val answerSwipe =
        View.OnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX = event.rawX

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val move = event.rawX - downX

                    if (move > 0) {

                        v.translationX = move
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {

                    val move = event.rawX - downX

                    if (move > 250) {

                        if (isUserBusy()) {

                            Toast.makeText(
                                this,
                                "User is busy now",
                                Toast.LENGTH_SHORT
                            ).show()

                            RingtoneHelper.stop()

                            v.animate()
                                .translationX(0f)
                                .setDuration(200)
                                .start()

                            return@OnTouchListener true
                        }

                        acceptCall()
                    }

                    v.animate()
                        .translationX(0f)
                        .setDuration(200)
                        .start()

                    true
                }

                else -> false
            }
        }

    private val rejectSwipe =
        View.OnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX = event.rawX

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val move = event.rawX - downX

                    if (move < 0) {

                        v.translationX = move
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {

                    val move = event.rawX - downX

                    if (move < -250) {

                        isUserResponse = "DECLINED"

                        isStartTime = getAcceptTime()

                        isEndTime = getAcceptTime()

                        isTotalDurationListened = 0L

                        isUpdateNotificationCall()

                        CallStateManager.markHandled(
                            this,
                            notificationId
                        )

                        stopEverything()

                        finishAndRemoveTask()
                    }

                    v.animate()
                        .translationX(0f)
                        .setDuration(200)
                        .start()

                    true
                }

                else -> false
            }
        }

    private fun acceptCall() {

        CallStateManager.markHandled(
            this,
            notificationId
        )

        isUserResponse = "ANSWERED"

        isStartTime = getAcceptTime()

        RingtoneHelper.stop()

        binding.answerContainer.visibility = View.GONE

        binding.rejectContainer.visibility = View.GONE

        binding.callControls.visibility = View.VISIBLE

        startAudio()
    }

    private fun startAudio() {

        player?.release()

        player =
            ExoPlayer.Builder(this)
                .build()

        // DEFAULT 50% VOLUME
        player?.volume = 0.5f

        val mediaItems =
            mutableListOf<MediaItem>()

        if (!welcomeUrl.isNullOrEmpty()) {

            mediaItems.add(
                MediaItem.fromUri(welcomeUrl!!)
            )
        }

        if (!mainUrl.isNullOrEmpty()) {

            mediaItems.add(
                MediaItem.fromUri(mainUrl!!)
            )
        }

        if (mediaItems.isEmpty()) {

            return
        }

        player?.setMediaItems(mediaItems)

        player?.prepare()

        player?.addListener(
            object : Player.Listener {

                override fun onPlaybackStateChanged(
                    state: Int
                ) {

                    if (state == Player.STATE_READY) {

                        totalDuration =
                            player?.duration ?: 0L

                        binding.tvDuration.text =
                            "00:00 / ${
                                formatTime(totalDuration)
                            }"
                    }

                    if (state == Player.STATE_ENDED) {

                        isEndTime = getAcceptTime()

                        isUserResponse = "COMPLETED"

                        stopEverything()

                        isUpdateNotificationCall()

                        finishAndRemoveTask()
                    }
                }
            }
        )

        player?.play()

        startTimer()
    }

    private fun startTimer() {

        binding.tvDuration.visibility =
            View.VISIBLE

        timerJob =
            lifecycleScope.launch {

                while (isActive) {

                    player?.let { exo ->

                        val currentPos =
                            exo.currentPosition

                        isTotalDurationListened =
                            currentPos

                        binding.tvDuration.text =
                            "${formatTime(currentPos)} / ${
                                formatTime(totalDuration)
                            }"
                    }

                    delay(500)
                }
            }
    }

    private fun togglePlayPause() {

        player?.let { exo ->

            if (exo.isPlaying) {

                exo.pause()

                isPaused = true

                binding.btnPlayPause.setImageResource(
                    R.drawable.play_icon_2
                )

            } else {

                exo.play()

                isPaused = false

                binding.btnPlayPause.setImageResource(
                    R.drawable.pause_icon_2
                )
            }
        }
    }

    private fun seekForward() {

        player?.let { exo ->

            val newPosition =
                exo.currentPosition + 5000

            if (newPosition < exo.duration) {

                exo.seekTo(newPosition)

            } else {

                exo.seekTo(exo.duration)
            }

            updateDurationLabel()
        }
    }

    private fun seekBackward() {

        player?.let { exo ->

            val newPosition =
                exo.currentPosition - 5000

            if (newPosition > 0) {

                exo.seekTo(newPosition)

            } else {

                exo.seekTo(0)
            }

            updateDurationLabel()
        }
    }

    private fun updateDurationLabel() {

        player?.let { exo ->

            binding.tvDuration.text =
                "${formatTime(exo.currentPosition)} / ${
                    formatTime(totalDuration)
                }"
        }
    }

    private fun stopEverything() {

        player?.let { exo ->

            isTotalDurationListened =
                exo.currentPosition

            Log.d(
                "FINAL_DURATION",
                "Sending duration: $isTotalDurationListened"
            )
        }

        RingtoneHelper.stop()

        timerJob?.cancel()

        player?.release()

        player = null

        stopService(
            Intent(
                this,
                CallForegroundService::class.java
            )
        )
    }

    private fun toggleSpeaker() {

        isSpeakerEnabled =
            !isSpeakerEnabled

        audioManager.isSpeakerphoneOn =
            isSpeakerEnabled

        if (isSpeakerEnabled) {

            // 100%
            player?.volume = 1.0f

            binding.btnSpeaker.setImageResource(
                R.drawable.ic_speaker_on
            )

        } else {

            // 50%
            player?.volume = 0.5f

            binding.btnSpeaker.setImageResource(
                R.drawable.ic_speaker_off
            )
        }
    }

    private fun formatTime(ms: Long): String {

        val totalSeconds = ms / 1000

        val minutes = totalSeconds / 60

        val seconds = totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    override fun onDestroy() {

        super.onDestroy()

        try {

            LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(closeReceiver)

        } catch (_: Exception) {
        }

        RingtoneHelper.stop()

        player?.release()

        player = null
    }

    private fun isUpdateNotificationCall() {

        var isStaffDetails: StaffDetails? =
            null

        isStaffDetails =
            SharedPreference.getStaffDetails(this)

        val isAccessToken =
            isStaffDetails!!.access_token

        if (isStartTime.equals("") ||
            isStartTime == null
        ) {

            isStartTime = getAcceptTime()
        }

        isEndTime = getAcceptTime()

        val isMobileNumber: String? =
            SharedPreference.getMobileNumber(this)

        val jsonObject = JsonObject()

        jsonObject.addProperty(
            "url",
            isIncomingCallData!!["url"]
        )

        jsonObject.addProperty(
            "duration",
            isTotalDurationListened / 1000
        )

        jsonObject.addProperty(
            "ei1",
            isIncomingCallData!!["ei5"]
        )

        jsonObject.addProperty(
            "ei2",
            isIncomingCallData!!["url"]
        )

        jsonObject.addProperty(
            "ei3",
            isAccessToken
        )

        jsonObject.addProperty(
            "ei4",
            "Android"
        )

        jsonObject.addProperty(
            "ei5",
            isIncomingCallData!!["ei5"]
        )

        jsonObject.addProperty(
            "start_time",
            isStartTime
        )

        jsonObject.addProperty(
            "end_time",
            isEndTime
        )

        jsonObject.addProperty(
            "retry_count",
            isIncomingCallData!!["retry_count"]
        )

        jsonObject.addProperty(
            "phone",
            isMobileNumber
        )

        jsonObject.addProperty(
            "receiver_id",
            isIncomingCallData!!["receiver_id"]
        )

        jsonObject.addProperty(
            "circular_id",
            isIncomingCallData!!["circular_id"]
        )

        jsonObject.addProperty(
            "diallist_id",
            isIncomingCallData!!["ei5"]
        )

        jsonObject.addProperty(
            "call_status",
            isUserResponse
        )

        Log.d(
            "jsonObjectReq",
            jsonObject.toString()
        )

        authViewModel!!
            .isUpdateNotificationCalllog(
                jsonObject,
                this
            )
    }

    private fun getAcceptTime(): String {

        val sdf =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            )

        return sdf.format(Date())
    }
}