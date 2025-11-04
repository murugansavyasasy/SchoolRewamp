package com.vs.schoolmessenger.FCM

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.NotificationCallScreenBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class NotificationCallScreen : BaseActivity<NotificationCallScreenBinding>(), View.OnClickListener {

    private var dX = 0f
    private var originalX = 0f
    private var isCallConnected = false
    private var isActivityClosing = false

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    var voiceUrl: String? = ""
    var isReceiverId: String? = ""
    var isUserResponse: String = "NO"
    var retrycount: String? = ""
    var circularId: String? = ""
    var ei1: String? = ""
    var ei2: String? = ""
    var ei3: String? = ""
    var ei4: String? = ""
    var ei5: String? = ""
    var role: String? = ""
    var menuId: String? = ""
    var notificationId: Int = 0

    var audioUrls: Array<String?>? = null

    var audioList: MutableList<String?>? = null
    var welcome_file: String? = ""
    var school_name: String? = ""
    var member_name: String? = ""
    var call_title: String? = ""
    private var updateRunnable: Runnable? = null
    private var totalElapsed = 0

    var isStartTime: String? = null
    var isEndTime: String? = null
    var isListeningDuration: String = "00:00"

    private var currentTrack = 0
    private var preparedCount = 0
    private var totalDurationMs: Long = 0
    private val trackDurations: MutableList<Int?> = java.util.ArrayList<Int?>()


    override fun getViewBinding(): NotificationCallScreenBinding {
        return NotificationCallScreenBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()
        startCallAnimation()

        handleIntent(getIntent())

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
                        movedDistance < -150 -> {
                            //end call
                            isUserResponse = "NO"
                            if (Constant.mediaPlayer.isPlaying()) {
                                Constant.mediaPlayer.stop()
                            }
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            isEndTime = sdf.format(Date())
                            isStartTime = isEndTime
                            updateNotificationCallLog(isStartTime!!, isEndTime!!)
                        }
                        else -> view.animate().x(originalX).setDuration(200).start()
                    }
                }
            }
            true
        }

        binding.callEndButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                mediaPlayer!!.stop()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                isEndTime = sdf.format(Date())
                isListeningDuration = binding.lblCurrentDuration.getText().toString()
                updateNotificationCallLog(isStartTime!!, isEndTime!!)
            }
        })


        // Step 1: Calculate total duration before playing
        calculateTotalDuration(Runnable {
            val formatted: String = formatDuration(totalDurationMs)
            Log.d("Total_Duration:", formatted)
            binding.lblTotalDuration.setText(formatted)
        })


        binding.declineButton.setOnClickListener {
            if (Constant.mediaPlayer.isPlaying()) {
                Constant.mediaPlayer.stop()
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            isEndTime = sdf.format(Date())
            isStartTime = isEndTime
            updateNotificationCallLog(isStartTime!!, isEndTime!!)
        }
    }


    private fun calculateTotalDuration(onComplete: Runnable) {
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

                tempPlayer.setOnPreparedListener(OnPreparedListener { mp: MediaPlayer? ->
                    val duration = mp!!.getDuration()
                    totalDurationMs += duration.toLong()
                    trackDurations.add(duration)
                    preparedCount++
                    mp.release()
                    if (preparedCount == audioUrls!!.size) {
                        onComplete.run()
                    }
                })

                tempPlayer.setOnErrorListener(MediaPlayer.OnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
                    preparedCount++
                    mp!!.release()
                    if (preparedCount == audioUrls!!.size) {
                        onComplete.run()
                    }
                    true
                })

                tempPlayer.prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun playAudio(index: Int) {
        binding.callEndButton.setVisibility(View.VISIBLE)
        if (index >= audioUrls!!.size) {
            Log.d("AUDIO_PLAYER", "All tracks completed")
            stopUpdatingProgress()

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

            mediaPlayer!!.setOnPreparedListener(OnPreparedListener { mp: MediaPlayer? ->
                Log.d(
                    "AUDIO_PLAYER", "Playing: " + (index + 1) +
                            " | Duration: " + formatDuration(mp!!.getDuration().toLong())
                )
                mp.start()
                startUpdatingProgress() // 🕒 start updating duration
            })

            mediaPlayer!!.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer? ->
                totalElapsed += mp!!.getDuration()
                currentTrack++
                if (currentTrack < audioUrls!!.size) {
                    playAudio(currentTrack)
                } else {
                    stopUpdatingProgress()
                    Log.d("AUDIO_PLAYER", "Playback finished")
                    releasePlayer()
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentTime = sdf.format(Date())
                    isEndTime = currentTime
                    isListeningDuration = binding.lblCurrentDuration.getText().toString()
                    updateNotificationCallLog(isStartTime!!, isEndTime!!)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    private fun startUpdatingProgress() {
        stopUpdatingProgress() // avoid duplicates

        updateRunnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null && mediaPlayer!!.isPlaying()) {
                    val currentPosition = mediaPlayer!!.getCurrentPosition()
                    val totalDuration = mediaPlayer!!.getDuration()
                    val totalProgress = totalElapsed + currentPosition // ✅ accumulated time

                    Log.d(
                        "AUDIO_PROGRESS", ("Current: " + formatDuration(totalProgress.toLong())
                                + " / Total: " + formatDuration(totalProgress.toLong()))
                    )

                    binding.lblCurrentDuration.setText(formatDuration(totalProgress.toLong()))
                }
                handler.postDelayed(this, 1000) // update every second
            }
        }
        handler.postDelayed(updateRunnable!!, 1000)
    }

    private fun updateNotificationCallLog(startTime: String?,endTime : String?){

    }

    private fun stopUpdatingProgress() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable!!)
        }
    }


    private fun formatDuration(durationMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun releasePlayer() {
        stopUpdatingProgress()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            notificationId = intent.getIntExtra("isNotificationId", -1)
            voiceUrl = intent.getStringExtra("isVoiceUrl")!!
            isReceiverId = intent.getStringExtra("isReceiverId")
            retrycount = intent.getStringExtra("retrycount")
            circularId = intent.getStringExtra("circularId")
            ei1 = intent.getStringExtra("ei1")
            ei2 = intent.getStringExtra("ei2")
            ei3 = intent.getStringExtra("ei3")
            ei4 = intent.getStringExtra("ei4")
            ei5 = intent.getStringExtra("ei5")
            role = intent.getStringExtra("role")
            menuId = intent.getStringExtra("menuId")

            welcome_file = intent.getStringExtra("welcome")
            school_name = intent.getStringExtra("school_name")
            member_name = intent.getStringExtra("member_name")
            call_title = intent.getStringExtra("call_title")

            binding.lblSchoolName.setText(school_name)
            binding.lblMemberName.setText("Calling - " + member_name + " from")
            binding.lblCallTitle.setText(call_title)


            audioList = ArrayList<String?>()
            if (welcome_file != "") {
                audioList!!.add(welcome_file)
            }
            audioList!!.add(voiceUrl)
            audioUrls = audioList!!.toTypedArray<String?>()
        }
    }

    private fun showConnectedState() {
        if (isCallConnected || isActivityClosing) return
        isCallConnected = true
        stopCallAnimation()
        binding.declineButton.setVisibility(View.GONE)
        binding.messageButton.setVisibility(View.GONE)


        binding.acceptButton.animate()
            .x(binding.actionContainer.getWidth() / 2f - binding.acceptButton.getWidth() / 2f)
            .setDuration(300)
            .withEndAction(Runnable {
                binding.ringContainer.setVisibility(View.GONE)
                binding.acceptButton.setVisibility(View.GONE)
                binding.callEndButton.setVisibility(View.VISIBLE)

                if (Constant.mediaPlayer.isPlaying()) {
                    Constant.mediaPlayer.stop()
                }
                isUserResponse = "OC"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                isStartTime = sdf.format(Date())
                isEndTime = "00:00"
                playAudio(currentTrack)
            })
            .start()
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

    override fun onClick(v: View?) {}

    override fun onDestroy() {
        super.onDestroy()
        stopCallAnimation()
        releasePlayer()
    }
}