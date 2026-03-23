package com.vs.schoolmessenger.FCM

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ActivityIncomingCallBinding
import kotlinx.coroutines.*

class IncomingCallActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var binding: ActivityIncomingCallBinding

    private lateinit var audioManager: AudioManager
    private var player: ExoPlayer? = null
    private var timerJob: Job? = null
    private var audioUrl: String? = null
    private var callAccepted = false
    private var callRejected = false
    private var isCallHandled = false
    private var downX = 0f
    private var isSpeakerEnabled = false
    private val autoCutHandler = Handler(Looper.getMainLooper())

    private var notificationId = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableLockScreen()

        notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioUrl = intent.getStringExtra("AUDIO_URL")
        val autoPlay = intent.getBooleanExtra("AUTO_PLAY", false)

        if (autoPlay) {
            if (isUserBusy()) {
                showBusyMessage()
                return
            }
            acceptCall()
        }

        startAutoCutTimer()
        setupClicks()
    }

    private fun setupClicks() {

        binding.btnAnswer.setOnTouchListener(answerSwipe)
        binding.btnReject.setOnTouchListener(rejectSwipe)

        binding.btnEndCall.setOnClickListener {
            callRejected = true
            isCallHandled = true
            endCall()
        }

        binding.btnSpeaker.setOnClickListener {
            toggleSpeaker()
        }

        binding.btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.video_play)
                } else {
                    it.play()
                    binding.btnPlayPause.setImageResource(R.drawable.pause_icon_2)
                }
            }
        }

        binding.btnForward.setOnClickListener {
            player?.seekTo((player?.currentPosition ?: 0) + 5000)
        }

        binding.btnBackward.setOnClickListener {
            val pos = (player?.currentPosition ?: 0) - 5000
            player?.seekTo(if (pos < 0) 0 else pos)
        }
    }

    private val answerSwipe = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val move = event.rawX - downX
                if (move > 0) v.translationX = move
                true
            }

            MotionEvent.ACTION_UP -> {
                val move = event.rawX - downX

                if (move > 300) {

                    if (isUserBusy()) {
                        showBusyMessage()

                    } else {
                        acceptCall()
                    }
                }

                v.animate().translationX(0f).setDuration(200).start()
                true
            }

            else -> false
        }
    }

    private val rejectSwipe = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val move = event.rawX - downX
                if (move < 0) v.translationX = move
                true
            }

            MotionEvent.ACTION_UP -> {
                val move = event.rawX - downX

                if (move < -300) {
                    callRejected = true
                    endCall()
                }

                v.animate().translationX(0f).setDuration(200).start()
                true
            }

            else -> false
        }
    }

    private fun acceptCall() {
        RingtoneHelper.stop()
        isCallHandled = true
        callAccepted = true

        CallStateManager.markHandled(this, notificationId)

        stopAutoCutTimer()

        binding.answerContainer.visibility = View.GONE
        binding.rejectContainer.visibility = View.GONE

        binding.callControls.visibility = View.VISIBLE
        binding.tvDuration.visibility = View.VISIBLE

        startTimer()
        audioUrl?.let { startAudio(it) }
    }
    private fun endCall() {
        RingtoneHelper.stop()
        stopAutoCutTimer()
        timerJob?.cancel()
        player?.release()
        player = null

        isCallHandled = true

        CallStateManager.markHandled(this, notificationId)

        finishAndRemoveTask()
    }

    private fun startAutoCutTimer() {
        autoCutHandler.postDelayed({

            if (!isCallHandled) {
                Log.d("MISSED_DEBUG", "Timer triggered")

                CallNotificationHelper.showMissedNotification(
                    this,
                    audioUrl,
                    notificationId
                )

                endCall()
            }
        }, 10000)
    }
    private fun stopAutoCutTimer() {
        autoCutHandler.removeCallbacksAndMessages(null)
    }

    private fun startAudio(url: String) {
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(url)

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        player?.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    endCall()
                }
            }
        })
    }

    private fun startTimer() {
        timerJob = launch {
            while (isActive) {
                player?.let {
                    val current = it.currentPosition
                    val total = it.duration
                    if (total > 0) {
                        binding.tvDuration.text =
                            "${formatTime(current)} / ${formatTime(total)}"
                    }
                }
                delay(500)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun toggleSpeaker() {
        isSpeakerEnabled = !isSpeakerEnabled
        audioManager.isSpeakerphoneOn = isSpeakerEnabled

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = if (isSpeakerEnabled) maxVolume else maxVolume / 2
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

        binding.btnSpeaker.setImageResource(
            if (isSpeakerEnabled) R.drawable.ic_speaker_on
            else R.drawable.ic_speaker_off
        )
    }

    private fun enableLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun isUserBusy(): Boolean {
        return audioManager.mode == AudioManager.MODE_IN_CALL ||
                audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    private fun showBusyMessage() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isKeyguardLocked) {
            binding.tvBusyMessage.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tvBusyMessage.visibility = View.GONE
            }, 2000)
        } else {
            Toast.makeText(
                this,
                "You are currently on another call.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}