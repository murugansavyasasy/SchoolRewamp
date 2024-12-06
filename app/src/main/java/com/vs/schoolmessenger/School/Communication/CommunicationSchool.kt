package com.vs.schoolmessenger.School.Communication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.CustomSwitch
import com.vs.schoolmessenger.Utils.WaveExtractor
import com.vs.schoolmessenger.Utils.WaveSeekBar
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private var isPlayingVoice = false // Track the playback state
    private lateinit var waveSeekBar: WaveSeekBar
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val audioUrl = "http://vs5.voicesnapforschools.com/nodejs/voice/VS_1718181818812.wav"
    private lateinit var audioPath: String
    private var lastPosition: Int = 0 // Variable to hold the last playback position


    //Recording
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioFile: File
    private var isRecording = false
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var recordingStartTime: Long = 0
    private val recordingHandler = Handler(Looper.getMainLooper())
    private val MAX_RECORDING_DURATION = 180000 // 3 minutes in milliseconds

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        // Underline text for labels
        binding.lblHistoryList.paintFlags =
            binding.lblHistoryList.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.lblBackToVoiceMessage.paintFlags =
            binding.lblBackToVoiceMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.rlaScheduleCall.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaFromTime.setOnClickListener(this)
        binding.rlaToTime.setOnClickListener(this)
        binding.imgVoicePlay.setOnClickListener(this)
        binding.imgVoiceRecord.setOnClickListener(this)

        val customSwitch: CustomSwitch = findViewById(R.id.SwitchEmergencyVoice)
        customSwitch.setOnClickListener {
            val state = if (customSwitch.isChecked()) "ON" else "OFF"
            Log.d("CommunicationSchool", "Switch state: $state")
            Toast.makeText(this, "Switch is $state", Toast.LENGTH_SHORT).show()
        }

        waveSeekBar = findViewById(R.id.waveSeekBar)
        audioPath = File(cacheDir, "audio.wav").absolutePath

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        // Download and extract audio
        downloadAudio(audioUrl, audioPath) {
            val waveExtractor = WaveExtractor()
            val waveHeights = waveExtractor.extractWaveHeights(audioPath, 100)
            waveSeekBar.setWaveHeights(waveHeights)
            Log.d("CommunicationSchool", "Audio wave heights loaded successfully.")
        }

        waveSeekBar.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP) {
                val width = waveSeekBar.width.toFloat()
                val progress = event.x / width
                waveSeekBar.setProgress(progress)

                mediaPlayer?.let {
                    val newPosition = (progress * it.duration).toInt()
                    it.seekTo(newPosition)
                    if (!it.isPlaying) it.start()
                }
                Log.d("CommunicationSchool", "Seek bar touched, progress: $progress")
            }
            true
        }

        if (lastPosition > 0) {
            mediaPlayer?.seekTo(lastPosition)
            mediaPlayer?.start()
            isPlayingVoice = true
            binding.imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.play_icon_voice
                )
            ) // Change icon to pause
        }

        if (isPlayingVoice) {
            waveSeekBar.setProgress(lastPosition.toFloat() / mediaPlayer!!.duration) // Set the initial progress
        }
    }

    private fun downloadAudio(url: String, outputPath: String, onComplete: () -> Unit) {
        Thread {
            try {
                val input = URL(url).openStream()
                val output = FileOutputStream(outputPath)
                input.copyTo(output)
                output.close()
                input.close()
                runOnUiThread { onComplete() }
                Log.d("CommunicationSchool", "Audio downloaded successfully to $outputPath")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunicationSchool", "Failed to download audio: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Failed to download audio", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioPath) // Ensure you are using the correct audio file path
                prepareAsync()
                setOnPreparedListener { mp ->
                    // If there was a previous position, seek to it before starting
                    if (lastPosition > 0) {
                        mp.seekTo(lastPosition)
                    }
                    mp.start() // Start playback
                    isPlayingVoice = true
                    updateSeekBar() // Start updating the seek bar
                    Log.d("CommunicationSchool", "MediaPlayer started.")
                }
                setOnCompletionListener {
                    Log.d(
                        "CommunicationSchool",
                        "Playback completed - entering completion listener."
                    )
                    lastPosition = 0 // Reset position on completion
                    isPlayingVoice = false

                    // Reset the wave seek bar to the beginning
                    waveSeekBar.setProgress(0f) // Set seek bar to the start
                    waveSeekBar.invalidate() // Force redraw to update UI

                    // Change icon back to play
                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CommunicationSchool,
                            R.drawable.play_icon_voice
                        )
                    )

                    // Stop any seek bar updates
                    handler.removeCallbacksAndMessages(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun togglePlayback() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                // Pause playback
                mp.pause()
                isPlayingVoice = false
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.play_icon_voice
                    )
                )
            } else {
                // Start or resume playback
                mp.start()
                isPlayingVoice = true
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.pause_icon
                    )
                )
                updateSeekBar() // Ensure seek bar updates continue
            }
        } ?: run {
            // If media player is null, initialize and start playback
            initializeMediaPlayer()
            setupWaveSeekBar()
            isPlayingVoice = true
            binding.imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pause_icon
                )
            )
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                val currentPosition = mp.currentPosition
                lastPosition = currentPosition // Store the current position
                if (mp.duration > 0) {
                    val progress = currentPosition.toFloat() / mp.duration
                    waveSeekBar.setProgress(progress) // Update the seek bar based on current position
                }
                // Schedule the next update
                handler.postDelayed({ updateSeekBar() }, 100)
            } else {
                handler.removeCallbacksAndMessages(null) // Stop seek bar updates
            }
        }
    }

    private fun setupWaveSeekBar() {
        // Assume you have a method to extract wave heights from audio
        val waveExtractor = WaveExtractor() // Replace with your actual wave extractor
        val waveHeights = waveExtractor.extractWaveHeights(
            audioUrl,
            100
        ) // Adjust the number of samples as needed
        waveSeekBar.setWaveHeights(waveHeights)
    }


    override fun onResume() {
        super.onResume()
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.seekTo(lastPosition) // Seek to the last position
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true) {
            lastPosition = mediaPlayer!!.currentPosition // Save the current position
            mediaPlayer?.pause() // Pause playback
            isPlayingVoice = false
            binding.imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.play_icon_voice
                )
            ) // Change icon to play
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaRecorder.release()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }
            R.id.rlaScheduleCall -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall,
                    binding.imgScheduleCall,
                    binding.lblScheduleCall
                )
            }
            R.id.rlaTextMessage -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }
            R.id.rlaFromTime -> {
                showSpinnerTimePicker(this) { hour, minute, isAm ->
                    val amPm = if (isAm) "AM" else "PM"
                    Toast.makeText(this, "Selected Time: $hour:$minute $amPm", Toast.LENGTH_SHORT)
                        .show()
                    binding.lblStartTime.text = "$hour:$minute $amPm"
                }
            }

            R.id.rlaToTime -> {
                showSpinnerTimePicker(this) { hour, minute, isAm ->
                    val amPm = if (isAm) "AM" else "PM"
                    Toast.makeText(this, "Selected Time: $hour:$minute $amPm", Toast.LENGTH_SHORT)
                        .show()
                    binding.lblEndTime.text = "$hour:$minute $amPm"
                }
            }

            R.id.imgVoicePlay -> {
                togglePlayback()
            }

            R.id.imgVoiceRecord -> {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
        }
    }

    private fun loadAudioFromFile() {
        val audioFilePath =
            "/storage/emulated/0/Android/data/com.vs.schoolmessenger/cache/recorded_audio.3gp"
        val audioFile = File(audioFilePath)

        if (audioFile.exists()) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audioFilePath) // Set the path to the audio file
                    prepareAsync() // Prepare the MediaPlayer asynchronously
                    setOnPreparedListener {
                        start() // Start playback when ready
                        binding.imgVoiceRecord.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CommunicationSchool,
                                R.drawable.pause_icon
                            )
                        )
                        Log.d("CommunicationSchool", "Audio playback started.")
                    }
                    setOnCompletionListener {
                        Log.d("CommunicationSchool", "Playback completed.")
                        // Reset or update UI as necessary
                        binding.imgVoiceRecord.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CommunicationSchool,
                                R.drawable.play_icon_voice
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("CommunicationSchool", "Error initializing MediaPlayer: ${e.message}")
                }
            }
        } else {
            Toast.makeText(this, "Audio file does not exist!", Toast.LENGTH_SHORT).show()
            Log.e("CommunicationSchool", "Audio file not found at: $audioFilePath")
        }
    }


    private fun updateRecordingDuration() {
        recordingHandler.postDelayed({
            if (isRecording) {
                val elapsedTime = System.currentTimeMillis() - recordingStartTime
                if (elapsedTime < MAX_RECORDING_DURATION) {
                    // Format elapsed time to mm:ss
                    val seconds = (elapsedTime / 1000).toInt()
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    binding.lblDurationOfVoice.text =
                        String.format("%02d:%02d / 03:00", minutes, remainingSeconds)

                    // Schedule the next update
                    updateRecordingDuration()
                } else {
                    // Stop recording if maximum duration is reached
                    stopRecording()
                    Toast.makeText(this, "Maximum recording duration reached.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, 1000) // Update every second
    }


    private fun startRecording() {
        binding.imgVoiceRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record))

        audioFile = File(externalCacheDir, "recorded_audio.3gp")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            prepare()
            start()
        }

        recordingStartTime = System.currentTimeMillis()
        updateRecordingDuration()

        isRecording = true
    }

    private fun stopRecording() {
        binding.imgVoiceRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.voice))
        mediaRecorder.apply {
            stop()
            release()
        }
        isRecording = false

        // Print the recording file URL
        val recordedAudioUrl = audioFile.absolutePath
        Log.d("Recording", "Recorded audio file URL: $recordedAudioUrl")
        recordingHandler.removeCallbacksAndMessages(null)
        // Optionally, display the URL in a Toast
        Toast.makeText(this, "Recorded audio file URL: $recordedAudioUrl", Toast.LENGTH_LONG).show()
        loadAudioFromFile()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
    }


    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        // Reset backgrounds and colors
        binding.rlaVoiceMessage.background = null
        binding.rlaScheduleCall.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background =
            ContextCompat.getDrawable(this, R.drawable.rect_sky_blue_shadow)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblScheduleCall.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Update icons based on selection
        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> {
                binding.imgVoiceMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.mic_icon
                    )
                )
            }
            binding.imgScheduleCall -> {
                binding.imgScheduleCall.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.call_schedule_icon
                    )
                )
            }
            binding.imgTextMessage -> {
                binding.imgTextMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.text_icon
                    )
                )
            }
        }
    }
}
