package com.vs.schoolmessenger.FCM

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NotificationCallScreenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationCallScreen :
    BaseActivity<NotificationCallScreenBinding>(), View.OnClickListener {

    private var dX = 0f
    private var originalX = 0f
    private var isCallConnected = false
    private var isActivityClosing = false
    private var isCallAccepted = false

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var voiceUrl: String? = null
    private var welcomeUrl: String? = null

    private var notificationId: Int = 0
    private var audioUrls: Array<String?>? = null
    private var audioList: MutableList<String?> = ArrayList()

    private var welcome_file: String? = ""
    private var school_name: String? = ""
    private var member_name: String? = ""
    private var call_title: String? = ""

    private var updateRunnable: Runnable? = null
    private var totalElapsed = 0
    private var currentTrack = 0
    private var preparedCount = 0
    private var totalDurationMs: Long = 0

    private var isStartTime: String? = null
    private var isEndTime: String? = null
    private var isListeningDuration = "00:00"
    private var authViewModel: Auth? = null

    private var ei1: String? = ""
    private var ei2: String? = ""
    private var ei3: String? = ""
    private var ei4: String? = ""
    private var ei5: String? = ""
    private var circular_id: String? = ""
    private var retrycount: String? = ""
    private var receiver_id: String? = ""
    private var isUserResponse: String? = "NO"



    override fun getViewBinding(): NotificationCallScreenBinding {
        return NotificationCallScreenBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()

        startCallAnimation()
        handleIntent(intent)
        setupSwipeActions()

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        binding.callEndButton.setOnClickListener { stopAndFinishCall() }
        binding.declineButton.setOnClickListener { endCallWithoutListening() }

        calculateTotalDuration {
            binding.lblTotalDuration.text = formatDuration(totalDurationMs)
        }

        authViewModel!!.isVersionCheck?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {

                }
            }
        }

    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        voiceUrl = intent.getStringExtra(Constant.isVoiceUrlNotifi)
        welcomeUrl = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
        notificationId = intent.getIntExtra(Constant.isNotificationId, -1)

        welcome_file = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
        school_name = intent.getStringExtra(Constant.school_name)
        member_name = intent.getStringExtra(Constant.member_name)
        call_title = intent.getStringExtra(Constant.call_title)

        ei1 = intent.getStringExtra(Constant.ei1)
        ei2 = intent.getStringExtra(Constant.ei2)
        ei3 = intent.getStringExtra(Constant.ei3)
        ei4 = intent.getStringExtra(Constant.ei4)
        ei5 = intent.getStringExtra(Constant.ei5)
        receiver_id = intent.getStringExtra(Constant.receiverid)
        retrycount = intent.getStringExtra(Constant.retrycount)
        circular_id = intent.getStringExtra(Constant.circular_id)

        binding.lblSchoolName.text = school_name
        binding.lblMemberName.text = "Calling - $member_name from"
        binding.lblCallTitle.text = call_title

        audioList.clear()

        // Add welcome first if valid
        if (!welcomeUrl.isNullOrEmpty() && welcomeUrl != "null" && welcomeUrl!!.isNotBlank()) {
            audioList.add(welcomeUrl)
        }
        // Add voice second if valid
        if (!voiceUrl.isNullOrEmpty() && voiceUrl != "null" && voiceUrl!!.isNotBlank()) {
            audioList.add(voiceUrl)
        }

        audioUrls = audioList.toTypedArray()
        Log.d("AUDIO_ORDER", "Audio order: $audioList")
    }

    private fun setupSwipeActions() {
        binding.acceptButton.setOnTouchListener { view, event ->
            if (isActivityClosing) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    originalX = view.x

                    //end call
                    isUserResponse = "NO"

                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    if (newX in binding.declineButton.x..binding.messageButton.x) {
                        view.x = newX
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val moved = view.x - originalX
                    when {
                        moved > 150 -> showConnectedState()
                        moved < -150 -> endCallWithoutListening()
                        else -> view.animate().x(originalX).setDuration(200).start()
                    }
                }
            }
            true
        }
    }

    private fun showConnectedState() {
        if (isCallConnected || isActivityClosing) return

        isCallConnected = true
        isCallAccepted = true

        stopCallAnimation()
        binding.declineButton.visibility = View.GONE
        binding.messageButton.visibility = View.GONE

        binding.acceptButton.animate()
            .x(binding.actionContainer.width / 2f - binding.acceptButton.width / 2f)
            .setDuration(300)
            .withEndAction {
                binding.ringContainer.visibility = View.GONE
                binding.acceptButton.visibility = View.GONE
                binding.callEndButton.visibility = View.VISIBLE

                if (Constant.mediaPlayer.isPlaying) Constant.mediaPlayer.stop()

                isStartTime = getNow()
                playAudio(currentTrack)
            }.start()

          isUserResponse = "OC"

    }

    private fun playAudio(index: Int) {
        if (audioUrls.isNullOrEmpty() || index >= audioUrls!!.size) {
            finishPlayback()
            return
        }

        releasePlayer()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer!!.setDataSource(audioUrls!![index])
            mediaPlayer!!.prepareAsync()

            mediaPlayer!!.setOnPreparedListener { mp ->
                mp.start()
                startUpdatingProgress()
            }

            mediaPlayer!!.setOnCompletionListener { mp ->
                totalElapsed += mp.duration
                currentTrack++
                playAudio(currentTrack)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            currentTrack++
            playAudio(currentTrack)
        }
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()

        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val current = totalElapsed + mp.currentPosition
                        binding.lblCurrentDuration.text = formatDuration(current.toLong())
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(updateRunnable!!, 1000)
    }

    private fun stopUpdatingProgress() {
        updateRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun calculateTotalDuration(onComplete: () -> Unit) {
        if (audioUrls.isNullOrEmpty()) return

        totalDurationMs = 0
        preparedCount = 0

        for (url in audioUrls!!) {
            val tempPlayer = MediaPlayer()
            try {
                tempPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                tempPlayer.setDataSource(url)
                tempPlayer.setOnPreparedListener { mp ->
                    totalDurationMs += mp.duration
                    preparedCount++
                    mp.release()
                    if (preparedCount == audioUrls!!.size) onComplete()
                }
                tempPlayer.prepareAsync()
            } catch (_: Exception) {
                preparedCount++
                tempPlayer.release()
                if (preparedCount == audioUrls!!.size) onComplete()
            }
        }
    }

    private fun finishPlayback() {
        stopUpdatingProgress()
        releasePlayer()
        isEndTime = getNow()
        isListeningDuration = binding.lblCurrentDuration.text.toString()
        updateNotificationCallLog(isStartTime!!, isEndTime!!)
    }

    private fun endCallWithoutListening() {
        if (Constant.mediaPlayer.isPlaying) Constant.mediaPlayer.stop()
        isStartTime = getNow()
        isEndTime = isStartTime
        updateNotificationCallLog(isStartTime!!, isEndTime!!)
    }

    private fun stopAndFinishCall() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isEndTime = getNow()
        isListeningDuration = binding.lblCurrentDuration.text.toString()
        updateNotificationCallLog(isStartTime!!, isEndTime!!)
    }

    private fun updateNotificationCallLog(start: String, end: String) {

        val MobileNumber: String? = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject()
        jsonObject.addProperty("url", voiceUrl)
        jsonObject.addProperty("duration", isListeningDuration)
        jsonObject.addProperty("ei1", ei1)
        jsonObject.addProperty("ei2", ei2)
        jsonObject.addProperty("ei3", ei3)
        jsonObject.addProperty("ei4", ei4)
        jsonObject.addProperty("ei5", ei5)
        jsonObject.addProperty("start_time", isStartTime)
        jsonObject.addProperty("end_time", isEndTime)
        jsonObject.addProperty("retry_count", retrycount)
        jsonObject.addProperty("phone", MobileNumber)
        jsonObject.addProperty("receiver_id", receiver_id)
        jsonObject.addProperty("circular_id", circular_id)
        jsonObject.addProperty("diallist_id", ei5)
        jsonObject.addProperty("call_status", isUserResponse)
        authViewModel!!.isUpdateNotificationCalllog(jsonObject, this)
        // TODO: call your API if needed
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing && isCallConnected) {
            mediaPlayer?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing && isCallConnected) {
            mediaPlayer?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isCallAccepted && isFinishing) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (ignored: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdatingProgress()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
    }

    private fun getNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatDuration(ms: Long): String {
        val min = TimeUnit.MILLISECONDS.toMinutes(ms)
        val sec = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
    }

    private fun startCallAnimation() {
        binding.ring1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.call_wave1))
        binding.ring2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.call_wave2))
        binding.ring3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.call_wave3))
    }

    private fun stopCallAnimation() {
        binding.ring1.clearAnimation()
        binding.ring2.clearAnimation()
        binding.ring3.clearAnimation()
        binding.ring1.visibility = View.GONE
        binding.ring2.visibility = View.GONE
        binding.ring3.visibility = View.GONE
    }

    override fun onClick(v: View?) {}
}
