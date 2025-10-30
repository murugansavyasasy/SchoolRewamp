package com.vs.schoolmessenger.FCM

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.NotificationCallScreenBinding


class NotificationCallScreen : BaseActivity<NotificationCallScreenBinding>(), View.OnClickListener {

    private var dX = 0f
    private var originalX = 0f
    private var isCallConnected = false
    private var isActivityClosing = false

    private var ringPlayer: MediaPlayer? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private var timerRunnable: Runnable? = null

    private var voiceUrl: String = "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/communication/7045/2025-09-266/RecordedAudio.m4a"

    override fun getViewBinding(): NotificationCallScreenBinding {
        return NotificationCallScreenBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()
        startCallAnimation()

        binding.acceptButton.setOnTouchListener { view, event ->
            if (isActivityClosing) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    originalX = view.x
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    if (newX in binding.declineButton.x..binding.messageButton.x) {
                        view.x = newX
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val movedDistance = view.x - originalX
                    when {
                        movedDistance > 150 -> showConnectedState()
                        movedDistance < -150 -> endCall()
                        else -> view.animate().x(originalX).setDuration(200).start()
                    }
                }
            }
            true
        }

        binding.declineButton.setOnClickListener {
            endCall()
        }

        binding.callEndButton.setOnClickListener {
            stopVoice()
            endCall()
        }
    }

    private fun startCallAnimation() {
        val wave1 = AnimationUtils.loadAnimation(this, R.anim.call_wave1)
        val wave2 = AnimationUtils.loadAnimation(this, R.anim.call_wave2)
        val wave3 = AnimationUtils.loadAnimation(this, R.anim.call_wave3)
        binding.ring1.startAnimation(wave1)
        binding.ring2.startAnimation(wave2)
        binding.ring3.startAnimation(wave3)
    }

    private fun stopCallAnimation() {
        binding.ring1.clearAnimation()
        binding.ring2.clearAnimation()
        binding.ring3.clearAnimation()
        binding.ring1.visibility = View.GONE
        binding.ring2.visibility = View.GONE
        binding.ring3.visibility = View.GONE
    }

    private fun showConnectedState() {
        if (isCallConnected || isActivityClosing) return
        isCallConnected = true

        stopCallAnimation()

        binding.callStatusText.text = "Connected"
        binding.slideText.visibility = View.GONE
        binding.declineButton.visibility = View.GONE
        binding.messageButton.visibility = View.GONE
        binding.callTimerText.visibility = View.VISIBLE
        binding.callTimerText.text = "00:00"

        binding.acceptButton.animate()
            .x(binding.actionContainer.width / 2f - binding.acceptButton.width / 2f)
            .setDuration(300)
            .withEndAction {
                binding.ringContainer.visibility = View.GONE
                binding.acceptButton.visibility = View.GONE

                binding.callEndButton.visibility = View.VISIBLE
                playVoice()
            }.start()
    }


    private fun playVoice() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(voiceUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    startTimer()
                }
                setOnCompletionListener {
                    stopTimer()
                    binding.callStatusText.text = "Call Ended"
                    Handler(Looper.getMainLooper()).postDelayed({
                        endCall()
                    }, 800)
                }
                setOnErrorListener { _, _, _ ->
                    binding.callStatusText.text = "Playback Error"
                    stopTimer()
                    endCall()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.callStatusText.text = "Playback Failed"
            stopTimer()
            endCall()
        }
    }

    private fun startTimer() {
        elapsedSeconds = 0
        timerRunnable = object : Runnable {
            override fun run() {
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.callTimerText.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun stopVoice() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopTimer()
    }

    private fun endCall() {
        if (isActivityClosing) return
        isActivityClosing = true
        stopVoice()
        stopCallAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 300)
    }

    override fun onClick(v: View?) {}

    override fun onDestroy() {
        super.onDestroy()
        stopCallAnimation()
        stopVoice()
    }
}