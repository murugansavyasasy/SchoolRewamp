package com.vs.schoolmessenger.FCM

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

class NotificationCallScreen : BaseActivity<NotificationCallScreenBinding>(), View.OnClickListener {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var voiceUrl: String? = null
    private var welcomeUrl: String? = null
    private var notificationId: Int = 0
    private var audioUrls: Array<String?>? = emptyArray()
    private var audioList: MutableList<String?> = ArrayList()
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
    private var isTotalDurationListened = 0
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
    private var isSpecker: Boolean? = false
    private var totalDurationCalculated = 0L

    private var isUserActionTaken = false

    override fun getViewBinding(): NotificationCallScreenBinding {
        return NotificationCallScreenBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()
//        val notificationId =
//            intent.getIntExtra(
//                "notification_id",
//                -1
//            )
//        if (notificationId != -1) {
//            NotificationManagerCompat
//                .from(this)
//                .cancel(notificationId)
//        }

//        Log.e("FSI_TEST", "AnnouncementActivity Opened")
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//            setTurnScreenOn(true)
//        }


        handleIntent(intent)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        binding.callEndButton.setOnClickListener { stopAndFinishCall() }
        authViewModel!!.isUpdateNotificationCallLog?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask()
                } else {
                    finishAffinity()
                }

                finish()
            }
        }

//        val launchSource =
//            intent.getStringExtra("launch_source")
//        Log.d(
//            "AnnouncementActivity",
//            "Source = $launchSource"
//        )
//        when (launchSource) {
//            "FULL_SCREEN" -> {
//                val isMissed =
//                    intent.getBooleanExtra(
//                        "is_missed_announcement",
//                        false
//                    )
//
//                if (!isMissed) {
//
//                    Log.d(
//                        "FSI_TEST",
//                        "Starting ringtone"
//                    )
//
//                    RingtonePlayer.stop()
//                    RingtonePlayer.start(this)
//
//                    handler.postDelayed(
//                        missedCallRunnable,
//                        30000
//                    )
//                }
//                // Opened automatically by Full Screen Intent
//            }
//            "ANSWER" -> {
//                // User tapped Accept button
//                showConnectedState()
//            }
//            "MISSED" -> {
//                // User tapped Missed Announcement notification
//                Log.d(
//                    "MISSED",
//                    "Notification opened"
//                )
//
//                RingtonePlayer.stop()
//            }
//        }
        binding.imgAcceptCall.setOnClickListener {
//            if(launchSource.equals("FULL_SCREEN")){
//                Log.d("Ringtone stopped","yes")
//                RingtonePlayer.stop()
//                isUserActionTaken = true
//
//                handler.removeCallbacks(missedCallRunnable)
//            }
//            NotificationManagerCompat
//                .from(this)
//                .cancel(1001)
            showConnectedState()
        }

        binding.imgDeclineCall.setOnClickListener {

//            RingtonePlayer.stop()
//
//            isUserActionTaken = true
//
//            handler.removeCallbacks(missedCallRunnable)
//
//            NotificationManagerCompat
//                .from(this)
//                .cancel(1001)

            endCallWithoutListening()
        }

        binding.imgSpecker.setOnClickListener {

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val halfVolume = maxVolume / 2

            if (isSpecker ?: false) {
                // Set to 50%
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    halfVolume,
                    0
                )
                binding.imgSpecker.setImageResource(R.drawable.volume_low)
                isSpecker = false
            } else {
                // Set to 100%
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    maxVolume,
                    0
                )
                binding.imgSpecker.setImageResource(R.drawable.volume_high)
                isSpecker = true
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        Log.d("Intent","values received")
        voiceUrl = intent.getStringExtra(Constant.isVoiceUrlNotifi)
        welcomeUrl = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
        notificationId = intent.getIntExtra(Constant.isNotificationId, -1)

        school_name = intent.getStringExtra(Constant.school_name)
        member_name = intent.getStringExtra(Constant.member_name)
        call_title = intent.getStringExtra(Constant.call_title)

        ei1 = intent.getStringExtra(Constant.ei1)
        ei2 = intent.getStringExtra(Constant.ei2)
        ei3 = intent.getStringExtra(Constant.ei3)
        ei4 = intent.getStringExtra(Constant.ei4)
        ei5 = intent.getStringExtra(Constant.ei5)
        receiver_id = intent.getStringExtra(Constant.isReceiverId)
        retrycount = intent.getStringExtra(Constant.retrycount)
        circular_id = intent.getStringExtra(Constant.circularId)

        Log.d("Circular_id", receiver_id + " " + circular_id)

        binding.lblSchoolName.text = school_name
        binding.lblMemberName.text = "Calling - $member_name from"
        binding.lblCallTitle.text = call_title

        audioList.clear()

        if (!welcomeUrl.isNullOrEmpty() && welcomeUrl != "null") {
            audioList.add(welcomeUrl)
        }
        if (!voiceUrl.isNullOrEmpty() && voiceUrl != "null") {
            audioList.add(voiceUrl)
        }

        audioUrls = audioList.toTypedArray()

        Log.d("AUDIO_ORDER", audioUrls!!.joinToString())
        Log.d("totalDurationMs", totalDurationMs!!.toString())

        calculateTotalDuration {
            binding.lblTotalDuration.text = formatDuration(totalDurationMs)
        }
    }

    private fun showConnectedState() {

        binding.lytAccept.visibility = View.GONE
        binding.lytDecline.visibility = View.GONE
        binding.rytCutCll.visibility = View.VISIBLE

        if (Constant.mediaPlayer.isPlaying) {
            Constant.mediaPlayer.stop()
        }

        totalElapsed = 0
        totalDurationCalculated = 0L
        currentTrack = 0
        binding.lblCurrentDuration.text = "00:00"
        isStartTime = getNow()

        // ▶ Start playback from first audio
        playAudio(currentTrack)

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

            Log.d("audioUrls!![index]",audioUrls!![index].toString())
            mediaPlayer!!.setDataSource(audioUrls!![index])
            mediaPlayer!!.prepareAsync()

            mediaPlayer!!.setOnPreparedListener { mp ->
                mp.start()
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val halfVolume = maxVolume / 2
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    halfVolume,
                    0
                )
                startUpdatingProgress()
            }

            mediaPlayer!!.setOnCompletionListener { mp ->
                totalElapsed += mp.duration
                currentTrack++
                playAudio(currentTrack)
            }

        } catch (e: Exception) {
            e.printStackTrace()
//            currentTrack++
//            playAudio(currentTrack)
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

        val validUrls = audioUrls?.filter { !it.isNullOrEmpty() } ?: emptyList()

        if (validUrls.isEmpty()) {
            totalDurationMs = 0
            onComplete()
            return
        }

        totalDurationMs = 0
        preparedCount = 0

        for (url in validUrls) {
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

                    if (preparedCount == validUrls.size) {
                        onComplete()
                    }
                }

                tempPlayer.setOnErrorListener { mp, _, _ ->
                    preparedCount++
                    mp.release()

                    if (preparedCount == validUrls.size) {
                        onComplete()
                    }
                    true
                }

                tempPlayer.prepareAsync()

            } catch (e: Exception) {
                preparedCount++
                tempPlayer.release()

                if (preparedCount == validUrls.size) {
                    onComplete()
                }
            }
        }
    }

    private fun finishPlayback() {

        stopUpdatingProgress()
        releasePlayer()

        isEndTime = getNow()
        isListeningDuration = binding.lblCurrentDuration.text.toString()
        isTotalDurationListened =
            durationToSeconds(binding.lblCurrentDuration.text.toString())

        updateNotificationCallLog(
            isStartTime!!,
            isEndTime!!
        )

        Handler(Looper.getMainLooper()).postDelayed({

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }

        }, 500)
    }
    private fun endCallWithoutListening() {
        if (Constant.mediaPlayer.isPlaying) Constant.mediaPlayer.stop()
        isStartTime = getNow()
        isEndTime = isStartTime
        updateNotificationCallLog(isStartTime!!, isEndTime!!)
    }

    private val missedCallRunnable = Runnable {

        Log.d("CALL_DEBUG", "missedCallRunnable fired")

        if (!isUserActionTaken) {

            Log.d("CALL_TIMEOUT", "User did not respond")

            RingtonePlayer.stop()

            NotificationManagerCompat
                .from(this)
                .cancel(1001)

            showMissedNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }
        }
    }

//    private val missedCallRunnable = Runnable {
//
//        if (!isUserActionTaken) {
//
//            Log.d("CALL_TIMEOUT", "User did not respond")
//
//            RingtonePlayer.stop()
//
//            NotificationManagerCompat
//                .from(this)
//                .cancel(1001)
//
//            showMissedNotification()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAndRemoveTask()
//            } else {
//                finishAffinity()
//            }
//        }
//    }

    private fun stopAndFinishCall() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isEndTime = getNow()
        isListeningDuration = binding.lblCurrentDuration.text.toString()
        isTotalDurationListened = durationToSeconds(binding.lblCurrentDuration.text.toString())
        Log.d("isTotalDurationListened", isTotalDurationListened.toString())
        updateNotificationCallLog(isStartTime!!, isEndTime!!)
    }

    private fun updateNotificationCallLog(start: String, end: String) {

        val MobileNumber: String? = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject()
        jsonObject.addProperty("url", voiceUrl)
        jsonObject.addProperty("duration", isTotalDurationListened)
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
        Log.d("jsonObjectReq", jsonObject.toString())
        authViewModel!!.isUpdateNotificationCalllog(jsonObject, this)
        // TODO: call your API if needed

    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            mediaPlayer?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) {
            mediaPlayer?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        RingtonePlayer.stop()
        if (isFinishing) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (ignored: Exception) {
            }
        }
    }

    private fun showMissedNotification() {

        RingtonePlayer.stop()
        val intent = Intent(
            this,
            NotificationCallScreen::class.java
        )

        intent.putExtras(getIntent())

        intent.putExtra(
            "launch_source",
            "MISSED"
        )
        intent.putExtra(
            "is_missed_announcement",
            true
        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                this,
                "school_chimes_notification"
            )
                .setSmallIcon(
                    android.R.drawable.sym_call_missed
                )
                .setContentTitle(
                    "Missed School Announcement"
                )
                .setContentText(
                    call_title ?: "Missed Announcement"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        NotificationManagerCompat
            .from(this)
            .notify(
                2001,
                notification
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        RingtonePlayer.stop()
        handler.removeCallbacks(missedCallRunnable)

        stopUpdatingProgress()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {
        }

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

    private fun durationToSeconds(time: String): Int {
        val parts = time.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return (minutes * 60) + seconds
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {
        }
        mediaPlayer = null
    }

    override fun onClick(v: View?) {}


    override fun onBackPressed() {

        RingtonePlayer.stop()

        NotificationManagerCompat
            .from(this)
            .cancel(1001)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finishAffinity()
        }
    }
}