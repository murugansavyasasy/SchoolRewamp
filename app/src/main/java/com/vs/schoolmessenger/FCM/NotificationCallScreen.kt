package com.vs.schoolmessenger.FCM

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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

    private var isEmergency: String? = null

    override fun getViewBinding(): NotificationCallScreenBinding {
        return NotificationCallScreenBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()

        val notificationId = intent.getIntExtra("notification_id", -1)
        isEmergency = intent.getStringExtra("isEmergencyCall")

        val circularId = intent.getStringExtra(Constant.circularId)

        if (!circularId.isNullOrEmpty()) {

            NotificationManagerCompat
                .from(this)
                .cancel(circularId.hashCode())

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.cancel(circularId.hashCode())
        }


        if (isEmergency== "1") {
            if (notificationId != -1) {
                NotificationManagerCompat
                    .from(this)
                    .cancel(notificationId)
            }

            Log.e("FSI_TEST", "AnnouncementActivity Opened")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            }
        } else {
            MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        binding.callEndButton.setOnClickListener { stopAndFinishCall() }
        authViewModel!!.isUpdateNotificationCallLog?.observe(this) { response ->
            if (response != null) {
                response.status
                response.message
                Log.d("update_call_log","update_call_log")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask()
                } else {
                    finishAffinity()
                }

                finish()
            }
        }

        handleIntent(intent)


        binding.imgAcceptCall.setOnClickListener {
            cancelMissedTimer()
            isUserActionTaken = true
            MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true

            handler.removeCallbacks(missedCallRunnable)

            RingtonePlayer.stop()

            circular_id?.let {
                NotificationManagerCompat
                    .from(this)
                    .cancel(it.hashCode())
            }
            showConnectedState()
        }

        binding.imgDeclineCall.setOnClickListener {

            cancelMissedTimer()

            isUserActionTaken = true
            MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true

            handler.removeCallbacks(missedCallRunnable)

            RingtonePlayer.stop()

            circular_id?.let {
                NotificationManagerCompat
                    .from(this)
                    .cancel(it.hashCode())
            }
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

    private fun cancelMissedTimer() {

        if (circular_id.isNullOrEmpty()) return

        synchronized(MyFirebaseMessagingService.activeMissedTimers) {

            MyFirebaseMessagingService.activeMissedTimers[circular_id]?.let {

                MyFirebaseMessagingService.handler.removeCallbacks(it)

                MyFirebaseMessagingService.activeMissedTimers.remove(circular_id)

                Log.e(
                    "MISSED_CALL",
                    "TIMER REMOVED : $circular_id"
                )
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val launchSource = intent.getStringExtra("launch_source")

        Log.d("Intent", "values received")
        voiceUrl = intent.getStringExtra(Constant.isVoiceUrlNotifi)
        welcomeUrl = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
        notificationId = intent.getIntExtra("notification_id", -1)

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
        isEmergency = intent.getStringExtra("isEmergencyCall")
        MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true
        cancelMissedTimer()
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

        getTotalDuration(audioList as List<String>) { duration ->
            binding.lblTotalDuration.text = formatDuration(duration)

            Log.d("TotalDuration", duration.toString())
        }

        if (isEmergency== "1") {
            Log.d(
                "AnnouncementActivity",
                "Source = $launchSource"
            )
            when (launchSource) {
                "FULL_SCREEN" -> {
                    val isMissed =
                        intent.getBooleanExtra(
                            "is_missed_announcement",
                            false
                        )

                    if (!isMissed) {

                        Log.d(
                            "FSI_TEST",
                            "Starting ringtone"
                        )

                        handler.postDelayed(
                            missedCallRunnable,
                            30000
                        )
                    }
                    // Opened automatically by Full Screen Intent
                }

                "ANSWER" -> {
                    // User tapped Accept button
                    showConnectedState()
                }

                "MISSED" -> {
                    // User tapped Missed Announcement notification
                    Log.d(
                        "MISSED",
                        "Notification opened"
                    )

                    RingtonePlayer.stop()
                }
            }

        }
    }

    private fun showConnectedState() {

        MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true

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
        MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true
        RingtonePlayer.stop()
        if (audioUrls.isNullOrEmpty() || index >= audioUrls!!.size) {
            Log.d("looping",index.toString())
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
                Log.d("Duration", mp.duration.toString())
                if (mp.duration <= 0) {
                    Log.d("Audio_Duration", "Invalid audio file")
                }
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val halfVolume = maxVolume / 2
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    halfVolume,
                    0
                )
                mp.start()
                startUpdatingProgress()
            }
            mediaPlayer!!.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayer", "onError what=$what extra=$extra")
                true
            }
            mediaPlayer!!.setOnInfoListener { mp, what, extra ->
                Log.d("MediaPlayer", "onInfo what=$what extra=$extra")
                false
            }

            mediaPlayer!!.setOnCompletionListener { mp ->
                Log.d("AudioCompleted","completed")
                totalElapsed += mp.duration
                currentTrack++
                MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true
                playAudio(currentTrack)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Audio_Exception",e.toString())
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

    private fun getTotalDuration(
        audioUrls: List<String>,
        callback: (Long) -> Unit
    ) {

        Thread {

            var totalDuration: Long = 0

            for (url in audioUrls) {

                val retriever = MediaMetadataRetriever()

                try {

                    retriever.setDataSource(url, HashMap())

                    val duration =
                        retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )

                    if (duration != null) {
                        totalDuration += duration.toLong()
                    }

                } catch (e: Exception) {
                    Log.e("Duration", "Failed for $url", e)
                } finally {
                    retriever.release()
                }
            }

            Handler(Looper.getMainLooper()).post {
                callback(totalDuration)
            }

        }.start()
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

        if (!isUserActionTaken) {

            Log.d("CALL_TIMEOUT", "User did not respond")

            // STOP RINGTONE
            RingtonePlayer.stop()

            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

            NotificationManagerCompat.from(this)
                .cancel(1001)

            if (!MyFirebaseMessagingService.isUserAnswered.isNotificationOpened){
                Log.d("idComing","isComing")
                showMissedNotification()
            }

            finishAndRemoveTask()
            finish()
        }
    }
    private fun stopAndFinishCall() {
        MyFirebaseMessagingService.isUserAnswered.isNotificationOpened=true
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

        if (isFinishing) {

            RingtonePlayer.stop()

            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {
            }
        }
    }

    private fun showMissedNotification() {

        RingtonePlayer.stop()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {
        }

        val intent = Intent(
            this,
            NotificationCallScreen::class.java
        )

        intent.putExtras(intent)

        intent.putExtra("launch_source", "MISSED")
        intent.putExtra("is_missed_announcement", true)

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
                "notification_school_chimes"
            )
                .setSmallIcon(android.R.drawable.sym_call_missed)
                .setContentTitle("Missed School Announcement")
                .setContentText(call_title ?: "Missed Announcement")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat
            .from(this)
            .notify(
                circular_id.hashCode(),
                notification
            )
    }

    override fun onDestroy() {

        try {
            cancelMissedTimer()

            MyFirebaseMessagingService.isUserAnswered.isNotificationOpened = true
            RingtonePlayer.stop()

            handler.removeCallbacksAndMessages(null)

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    private fun getNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatDuration(ms: Long): String {
//        val min = TimeUnit.MILLISECONDS.toMinutes(ms)
//        val sec = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
//        return String.format("%02d:%02d", min, sec)

        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun durationToSeconds(time: String): Int {
        val parts = time.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return (minutes * 60) + seconds
    }

    private fun releasePlayer() {
//        try {
//            mediaPlayer?.stop()
//            mediaPlayer?.release()
//        } catch (ignored: Exception) {
//        }
//        mediaPlayer = null

        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Release error", e)
        } finally {
            mediaPlayer = null
        }
    }

    override fun onClick(v: View?) {}


    override fun onBackPressed() {

        RingtonePlayer.stop()

        circular_id?.let {
            NotificationManagerCompat
                .from(this)
                .cancel(it.hashCode())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finishAffinity()
        }
    }
}