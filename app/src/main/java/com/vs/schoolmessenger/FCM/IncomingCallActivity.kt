package com.vs.schoolmessenger.FCM

import android.app.*
import android.content.Context
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ActivityIncomingCallBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncomingCallActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var binding: ActivityIncomingCallBinding
    private lateinit var audioManager: AudioManager
    private var isUserResponse: String? = "NO"
    private var isStartTime: String? = null
    private var isEndTime: String? = null
    private var player: ExoPlayer? = null
    private var timerJob: Job? = null
    private var callAccepted = false
    private var callRejected = false
    private var isCallHandled = false
    private var downX = 0f
    private var isSpeakerEnabled = false
    private val autoCutHandler = Handler(Looper.getMainLooper())
    private var authViewModel: Auth? = null
    private var notificationId = 1001
    private var welcomeUrl: String? = null
    private var mainUrl: String? = null
    private var isTotalDurationListened = 0L
    private var totalDuration = 0L
    private var isIncomingCallData: HashMap<String, String>? = null
    var welcomeDuration = 0L
    var isUpdateCallApi=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        enableLockScreen()

        isIncomingCallData = intent.getSerializableExtra("DATA") as? HashMap<String, String>

        welcomeUrl = isIncomingCallData?.get("welcome")
        mainUrl = isIncomingCallData?.get("url")
        notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val autoPlay = intent.getBooleanExtra("AUTO_PLAY", false)

        if (autoPlay) {
            if (isUserBusy()) {
                showBusyMessage()
                return
            }
            acceptCall()
        }

        authViewModel!!.isUpdateNotificationCallLog?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message
                finish()
            }
        }

        startAutoCutTimer()
        setupClicks()
    }

    private fun isUpdateNotificationCall() {

         var isStaffDetails: StaffDetails? = null
         isStaffDetails = SharedPreference.getStaffDetails(this)
         val isAccessToken = isStaffDetails!!.access_token

        if (isStartTime.equals("") || isStartTime==null) {
            isStartTime = getAcceptTime()
        }
        isEndTime=getAcceptTime()

        val isMobileNumber: String? = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject()
        jsonObject.addProperty("url", isIncomingCallData!!["url"])
        jsonObject.addProperty("duration", isTotalDurationListened / 1000)
        jsonObject.addProperty("ei1", isIncomingCallData!!["ei5"])
        jsonObject.addProperty("ei2", isIncomingCallData!!["url"])
        jsonObject.addProperty("ei3", isAccessToken)
        jsonObject.addProperty("ei4", "Android")
        jsonObject.addProperty("ei5", isIncomingCallData!!["ei5"])
        jsonObject.addProperty("start_time", isStartTime)
        jsonObject.addProperty("end_time", isEndTime)
        jsonObject.addProperty("retry_count", isIncomingCallData!!["retry_count"])
        jsonObject.addProperty("phone", isMobileNumber)
        jsonObject.addProperty("receiver_id", isIncomingCallData!!["receiver_id"])
        jsonObject.addProperty("circular_id", isIncomingCallData!!["circular_id"])
        jsonObject.addProperty("diallist_id", isIncomingCallData!!["ei5"])
        jsonObject.addProperty("call_status", isUserResponse)
        Log.d("jsonObjectReq", jsonObject.toString())
        authViewModel!!.isUpdateNotificationCalllog(jsonObject, this)
    }

    private fun getAcceptTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupClicks() {

        binding.btnAnswer.setOnTouchListener(answerSwipe)
        binding.btnReject.setOnTouchListener(rejectSwipe)

        binding.btnEndCall.setOnClickListener {
            callRejected = true
            isCallHandled = true
            isUpdateCallApi=true
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
                    isUpdateCallApi=true
                    endCall()
                }

                v.animate().translationX(0f).setDuration(200).start()
                true
            }

            else -> false
        }
    }

    private fun acceptCall() {

        isStartTime = getAcceptTime()
        isUserResponse = "OG"

        RingtoneHelper.stop()
        isCallHandled = true
        callAccepted = true

        CallStateManager.markHandled(this, notificationId)
        stopAutoCutTimer()

        binding.answerContainer.visibility = View.GONE
        binding.rejectContainer.visibility = View.GONE
        binding.callControls.visibility = View.VISIBLE
        binding.tvDuration.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val welcomeDur = welcomeUrl?.let { getAudioDuration(it) } ?: 0L
            val mainDur = mainUrl?.let { getAudioDuration(it) } ?: 0L

            welcomeDuration = welcomeDur
            totalDuration = welcomeDur + mainDur

            withContext(Dispatchers.Main) {
                startAudio()
                startTimer()
            }
        }
    }

    private fun endCall() {
        player?.let { exo ->

            val currentPos = exo.currentPosition
            val currentIndex = exo.currentMediaItemIndex

            isTotalDurationListened = when (currentIndex) {
                0 -> currentPos
                1 -> welcomeDuration + currentPos
                else -> currentPos
            }

            Log.d("FINAL_DURATION", "Sending duration: $isTotalDurationListened")
        }

        RingtoneHelper.stop()
        stopAutoCutTimer()
        timerJob?.cancel()
        player?.release()
        player = null

        isCallHandled = true

        CallStateManager.markHandled(this, notificationId)

        if (isUpdateCallApi) {
            isUpdateNotificationCall()
        }

        finishAndRemoveTask()
    }

    private fun getAudioDuration(url: String): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(url, HashMap())
            val durationStr =
                retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun startAutoCutTimer() {
        autoCutHandler.postDelayed({

            if (!isCallHandled) {
                Log.d("MISSED_DEBUG", "Timer triggered")

                CallNotificationHelper.showMissedNotification(
                    this,
                    isIncomingCallData!!,
                    notificationId
                )
                isUpdateCallApi=false
                endCall()
            }
        }, 10000)
    }
    private fun stopAutoCutTimer() {
        autoCutHandler.removeCallbacksAndMessages(null)
    }

    private fun startAudio() {

        player?.release()

        player = ExoPlayer.Builder(this).build()

        val mediaItems = mutableListOf<MediaItem>()
        if (!welcomeUrl.isNullOrEmpty()) {
            mediaItems.add(MediaItem.fromUri(welcomeUrl!!))
        }

        if (!mainUrl.isNullOrEmpty()) {
            mediaItems.add(MediaItem.fromUri(mainUrl!!))
        }
        if (mediaItems.isEmpty()) {
            return
        }

        player?.setMediaItems(mediaItems)
        player?.prepare()
        player?.play()

        player?.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {

                if (state == com.google.android.exoplayer2.Player.STATE_READY) {
                    Log.d("AUDIO_DEBUG", "Player READY")
                }

                if (state == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    Log.d("AUDIO_DEBUG", "Playback completed")
                    isUpdateCallApi=true
                    endCall()
                }
            }
        })
    }
    private fun startTimer() {
        timerJob = launch {
            while (isActive) {

                player?.let { exo ->

                    val currentPos = exo.currentPosition
                    val currentIndex = exo.currentMediaItemIndex

                    val totalPlayed = when (currentIndex) {
                        0 -> currentPos
                        1 -> welcomeDuration + currentPos
                        else -> currentPos
                    }

                    binding.tvDuration.text =
                        "${formatTime(totalPlayed)} / ${formatTime(totalDuration)}"

                    isTotalDurationListened = totalPlayed
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