package com.vs.schoolmessenger.School.Communication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomSwitch
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener,
    VoiceHistoryClickListener, TextHistoryClickListener, TimeSelectedListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private lateinit var selectedDatesAdapter: SelectedDatesAdapter
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null
    private var isClickType = 1
    private var isPlayingVoice = false // Track the playback state
    private var lastPosition: Int = 0 // Variable to hold the last playback position
    private val PERMISSIONS_REQUEST_CODE = 200
    var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
     var mAdapter: VoiceHistoryAdapter?=null
     var mTextAdapter: TextHistoryAdapter?=null
    private lateinit var isVoiceHistoryData: List<VoiceHistoryData>
    private lateinit var isTextHistoryData: List<TextHistoryData>
    private val MAX_RECORDING_TIME = 180
    private val handler = Handler(Looper.getMainLooper())
    private val progressUpdater = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer!!.isPlaying) {
//                val progress = mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration
//                binding.waveformSeekBar.progress = progress // Ensure this updates correctly
                handler.postDelayed(this, 100) // Update every 100ms
            }
        }
    }
    private val selectedDates = mutableSetOf<String>()

    private var recordingTime = 0 // Recording time in seconds
    private lateinit var recordingHandler: Handler
    private lateinit var recordingRunnable: Runnable
    private val calendar = Calendar.getInstance()
    private lateinit var dateAdapter: DateAdapter
    private lateinit var popupWindow: PopupWindow

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        // Underline text for labels
        binding.lblHistoryList.paintFlags =
            binding.lblHistoryList.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.lblBackToVoiceMessage.paintFlags =
            binding.lblBackToVoiceMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Set up listeners for UI elements
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.rlaScheduleCall.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaFromTime.setOnClickListener(this)
        binding.rlaToTime.setOnClickListener(this)
        binding.imgVoicePlay.setOnClickListener(this)
        binding.imgVoiceRecord.setOnClickListener(this)
        binding.lnrHistoryList.setOnClickListener(this)
        binding.rlaBackRecord.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.lnrScheduleCall.setOnClickListener(this)


        checkAndRequestPermissions()
        audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord.3gp"

        val customSwitch: CustomSwitch = findViewById(R.id.SwitchEmergencyVoice)
        customSwitch.setOnClickListener {
            val state = if (customSwitch.isChecked()) "ON" else "OFF"
            Toast.makeText(this, "Switch is $state", Toast.LENGTH_SHORT).show()
        }

        // Initialize handler for updating recording time
        recordingHandler = Handler()
        recordingRunnable = Runnable {
            if (isRecording) {
                recordingTime++
                binding.lblDurationOfVoice.text = String.format(
                    "%02d:%02d" + " / 03:00",
                    recordingTime / 60,
                    recordingTime % 60
                )

                if (recordingTime >= MAX_RECORDING_TIME) {
                    stopRecording()
                } else {
                    recordingHandler.postDelayed(recordingRunnable, 1000) // Update every second
                }
            }
        }
    }


    private fun startRecording() {
        if (checkAndRequestPermissions()) {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    binding.imgVoiceRecord.setImageDrawable(
                        ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_voice)
                    )
                    prepare()
                    start()
                    isRecording = true
                    recordingTime = 0 // Reset recording time
                    recordingHandler.post(recordingRunnable) // Start updating time
                    Toast.makeText(
                        this@CommunicationSchool,
                        "Recording started",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@CommunicationSchool, "Recording failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            checkAndRequestPermissions()
        }
    }

    private fun stopRecording() {
        val parts = binding.lblDurationOfVoice.text.toString().split(" / ")
        if (parts.isNotEmpty()) {
            val currentDuration = parts[0]
            binding.lblEndDuration.text = currentDuration
        }
        //        loadWaveform()
        mediaRecorder?.apply {
            binding.imgVoiceRecord.setImageDrawable(
                ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_icon)
            )
            stop()
            release()
            mediaRecorder = null
            isRecording = false
            recordingHandler.removeCallbacks(recordingRunnable) // Stop updating time
            Toast.makeText(this@CommunicationSchool, "Recording stopped", Toast.LENGTH_SHORT).show()
            Log.d(
                "RecordingFilePath",
                "Recording stopped. File Path: $audioFilePath"
            ) // Print the file path when recording stops
            binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
        }
    }

    private fun initializeMediaPlayer() {
        if (audioFilePath.isNullOrEmpty()) {
            Log.e("MediaPlayerError", "Audio file path is null or empty")
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepareAsync() // Prepare asynchronously

            setOnPreparedListener {
                isPrepared = true
                startAudioProgressUpdate() // Start updating progress
                start() // Start playback
                updateCurrentTime() // Start updating current time
            }

            setOnCompletionListener {

                stopAudioProgressUpdate()
                lastPosition = 0 // Reset last position on completion
                isPlayingVoice = false // Update playback state
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.video_play)
                ) // Change icon to play
                binding.lblStartDuration.text = "00:00"
            }
        }
    }


    private fun updateCurrentTime() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        binding.lblStartDuration.text = formatDuration(currentPosition)
                        handler.postDelayed(this, 1000) // Schedule the next update
                    }
                }
            }
        }, 1000)
    }

    private fun formatDuration(durationInMillis: Int): String {
        val minutes = (durationInMillis / 1000) / 60
        val seconds = (durationInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun startAudioProgressUpdate() {
        handler.post(progressUpdater)
        binding.waveformSeekBar.invalidate() // Force redraw
    }

    private fun stopAudioProgressUpdate() {
        handler.removeCallbacks(progressUpdater)
        binding.waveformSeekBar.updateWithLevel(0f)
        binding.imgVoicePlay.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.video_play)
        )
    }

    override fun onResume() {
        super.onResume()
        if (checkAndRequestPermissions()) {
            println("Permissions granted after returning from settings.") // Debug log
        } else {
            println("Permissions still denied after returning from settings.") // Debug log
        }
        // Consider restoring playback or UI state if necessary
    }


    override fun onPause() {
        super.onPause()
        if (mAdapter != null) {
            mAdapter!!.releaseMediaPlayer()
        }


        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                lastPosition = player.currentPosition // Save the current position
                player.pause() // Pause playback
                isPlayingVoice = false
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.video_play)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release() // Safe release to avoid NullPointerException
        recordingHandler.removeCallbacks(recordingRunnable)
        stopAudioProgressUpdate()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                isClickType = 1
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "Back to voice message"

                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.GONE
                binding.rlaMessageFromText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE

                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }

            R.id.rlaScheduleCall -> {
                isClickType = 2
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "Back to voice message"

                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.gridViewScheduleCall.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                binding.rlaMessageFromText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall,
                    binding.imgScheduleCall,
                    binding.lblScheduleCall
                )
            }

            R.id.rlaTextMessage -> {
                isClickType = 3
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "Back to text message"
                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.rlaMessageFromText.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.GONE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }

            R.id.rlaFromTime -> {
//                showSpinnerTimePicker(this) { hour, minute, isAm ->
//                    val amPm = if (isAm) "AM" else "PM"
//                    Toast.makeText(this, "Selected Time: $hour:$minute $amPm", Toast.LENGTH_SHORT)
//                        .show()
//                    binding.lblStartTime.text = "$hour:$minute $amPm"

                    showTimePickerDialog(this,this)


             //   }
            }

            R.id.rlaToTime -> {
//                showSpinnerTimePicker(this) { hour, minute, isAm ->
//                    val amPm = if (isAm) "AM" else "PM"
//                    Toast.makeText(this, "Selected Time: $hour:$minute $amPm", Toast.LENGTH_SHORT)
//                        .show()
                showTimePickerDialog(this, this)
                //     }
            }

            R.id.imgVoicePlay -> {

                if (isPlayingVoice) {
                    // Pause the media player
                    mediaPlayer?.let {
                        it.pause()
                        lastPosition = it.currentPosition // Save current position
                        isPlayingVoice = false
                        binding.imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.video_play
                            ) // Change icon to play
                        )
                    }
                } else {
                    // If the media player is not initialized, initialize it
                    if (!isPrepared) {
                        val normalizedPower = max(1f, (1f + 160) / 160)
                        binding.waveformSeekBar.updateWithLevel(normalizedPower)
                        initializeMediaPlayer() // Prepare the media player for the first time
                        binding.imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            ) // Change icon to pause
                        )
                        isPlayingVoice = true
                    } else {
                        val normalizedPower = max(1f, (1f + 160) / 160)
                        binding.waveformSeekBar.updateWithLevel(normalizedPower)
                        // Resume playback from the last position
                        mediaPlayer?.let {
                            it.seekTo(lastPosition) // Seek to last position
                            it.start() // Start playing
                            isPlayingVoice = true
                            binding.imgVoicePlay.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.pause_icon
                                ) // Change icon to pause
                            )
                            startAudioProgressUpdate() // Start updating progress again
                        }
                    }
                }
            }

            R.id.imgVoiceRecord -> {
                if (!isRecording) {
                    startRecording()
                } else {
                    stopRecording()
                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrScheduleCall -> {
                showDatePickerPopup()
            }

            R.id.rlaBackRecord -> {
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }

                Log.d("isClickType", isClickType.toString())
                when (isClickType) {
                    1 -> {
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaScheduleCallPickDate.visibility = View.GONE
                    }

                    2 -> {
                        binding.gridViewScheduleCall.visibility = View.VISIBLE
                        binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                        binding.rlaRecordVoice.visibility = View.VISIBLE

                    }

                    else -> {
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaScheduleCallPickDate.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.VISIBLE
                    }
                }

                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE


            }

            R.id.lnrHistoryList -> {
                when (isClickType) {
                    1 -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        loadData()
                    }

                    2 -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.gridViewScheduleCall.visibility = View.VISIBLE
                        loadData()
                    }

                    else -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        loadTextData()
                    }
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

        binding.imgVoiceMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.mic_icon_black
            )
        )

        binding.imgScheduleCall.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.call_schedule_icon_black
            )
        )

        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.text_icon_black
            )
        )

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


    private fun loadData() {
        isVoiceHistoryData = listOf(
            VoiceHistoryData(
                "Annual Day celebrations",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            ),
            VoiceHistoryData(
                "Parent Meeting",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            ),
            VoiceHistoryData(
                "Normal Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            ),
            VoiceHistoryData(
                "Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3"
            ),
            VoiceHistoryData(
                "Monday",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_1MG.mp3"
            ),
            VoiceHistoryData(
                "Nothing",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav"
            ),
            VoiceHistoryData(
                "Value Education",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
            ),
            VoiceHistoryData(
                "Environmental Science",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://filesamples.com/samples/audio/mp3/sample4.mp3"
            ),
            VoiceHistoryData(
                "Okay okay",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://ia800304.us.archive.org/8/items/testmp3testfile/mpthreetest.mp3"
            )
        )

        mAdapter = VoiceHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false;
        binding.rcyHistoryDataVoiceAndText.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                VoiceHistoryAdapter(isVoiceHistoryData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyHistoryDataVoiceAndText.adapter = mAdapter
        }
    }

    private fun loadTextData() {
        isTextHistoryData = listOf(
            TextHistoryData(
                "Annual Day celebrations",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Parent Meeting",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Normal Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Monday",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Nothing",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Value Education",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Environmental Science",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextHistoryData(
                "Okay okay",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            )
        )

        mTextAdapter = TextHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false;
        binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mTextAdapter =
                TextHistoryAdapter(isTextHistoryData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val requiredPermissions = mutableListOf<String>()

        // Check each permission and add to the list if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Request permissions if needed
        return if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
            false // Indicate that permissions are not yet granted
        } else {
            true // All required permissions are already granted
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val deniedPermissions = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (deniedPermissions.isNotEmpty()) {
                // Check if the user has permanently denied any permission
                val permanentlyDenied = deniedPermissions.filter {
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }

                if (permanentlyDenied.isNotEmpty()) {
                    // Inform the user that they need to enable permissions in settings
                    Toast.makeText(
                        this,
                        "Please enable permissions from app settings.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Inform the user that permissions were denied
                    Toast.makeText(
                        this,
                        "Required permissions denied: $deniedPermissions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // All permissions were granted
                Toast.makeText(this, "All permissions granted.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onItemClick(data: VoiceHistoryData, holder: VoiceHistoryAdapter.DataViewHolder) {

    }

    override fun onItemClick(data: TextHistoryData, holder: TextHistoryAdapter.DataViewHolder) {

    }

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblStartTime.text = String.format("%02d:%02d %s", hour, minute, amPm)
        binding.lblEndTime.text = String.format("%02d:%02d %s", hour, minute, amPm)

    }


    private fun showDatePickerPopup() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.dialog_multi_date_picker, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable
        )

        // Dim the background when the popup is displayed
        dimBehind(popupWindow)

        // Initialize views from the popup layout
        val prevMonthButton = popupView.findViewById<ImageView>(R.id.prevMonthButton)
        val nextMonthButton = popupView.findViewById<ImageView>(R.id.nextMonthButton)
        val currentMonthText = popupView.findViewById<TextView>(R.id.currentMonthText)
        val confirmButton = popupView.findViewById<TextView>(R.id.confirmButton)
        val dateRecyclerView = popupView.findViewById<RecyclerView>(R.id.dateRecyclerView)

        // Initialize the DateAdapter
        dateAdapter = DateAdapter(this) { selectedDatesList ->
            // You can handle additional selections here if needed
        }

        dateRecyclerView.layoutManager = GridLayoutManager(this, 7) // 7 columns for the week
        dateRecyclerView.adapter = dateAdapter

        // Load current month dates
        loadDates(currentMonthText)

        // Update the DateAdapter to reflect current selected dates
        dateAdapter.setSelectedDates(selectedDates.toList())

        // Initialize selectedDatesAdapter and pass dateAdapter reference
        selectedDatesAdapter = SelectedDatesAdapter(this, selectedDates.toMutableList(), dateAdapter) { removedDate ->
            dateAdapter.removeSelectedDate(removedDate)
        }

        // Set the adapter for your GridView or RecyclerView where you display selected dates
        binding.gridViewScheduleCall.adapter = selectedDatesAdapter

        // Highlight previously selected dates in DateAdapter
        selectedDates.forEach { date ->
            dateAdapter.selectDate(date)
        }

        // Set up button click listeners
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            loadDates(currentMonthText)
            dateAdapter.setSelectedDates(selectedDates.toList())
        }

        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            loadDates(currentMonthText)
            dateAdapter.setSelectedDates(selectedDates.toList())
        }

        confirmButton.setOnClickListener {
            clearDim()
            val selectedDatesList = dateAdapter.getSelectedDates().toMutableList()

            // Update the selected dates in the SelectedDatesAdapter
            selectedDates.clear()
            selectedDates.addAll(selectedDatesList)

            // Notify the selectedDatesAdapter of changes
            selectedDatesAdapter.submitSelectedDates(selectedDates.toList()) // Notify adapter of changes

            popupWindow.dismiss() // Dismiss the popup
        }

        // Show the popup at the center of the screen
        popupWindow.showAtLocation(window.decorView.rootView, Gravity.CENTER, 0, 0)

        // Set a transparent background for the popup window
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Clear the dimming effect when the popup is dismissed
        popupWindow.setOnDismissListener {
            clearDim()
        }
    }

    private fun loadDates(currentMonthText: TextView) {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        currentMonthText.text = dateFormat.format(calendar.time)

        val today = Calendar.getInstance()
        val endDate = today.clone() as Calendar
        endDate.add(Calendar.DAY_OF_MONTH, 6)

        val dates = mutableListOf<DateItem>()
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

        // Add empty cells for days before the first day of the month
        for (i in 1 until firstDayOfWeek) {
            dates.add(DateItem(null, false)) // Empty cells
        }

        // Add the dates of the month
        for (i in 1..daysInMonth) {
            val currentDate = calendar.clone() as Calendar
            currentDate.set(Calendar.DAY_OF_MONTH, i)

            // Check if the date is in the selectable range
            val isSelectable = !currentDate.before(today) && !currentDate.after(endDate)
            dates.add(DateItem(i, isSelectable))
        }

        dateAdapter.submitDates(dates)
    }
}


// Don't delete below code

/**
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
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomSwitch
import com.vs.schoolmessenger.Utils.SeekBarOnProgressChanged
import com.vs.schoolmessenger.Utils.WaveExtractor
import com.vs.schoolmessenger.Utils.WaveformSeekBar
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.IOException
import java.util.Random

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener,
VoiceHistoryClickListener, TextHistoryClickListener {

override fun getViewBinding(): CommunicationSchoolBinding {
return CommunicationSchoolBinding.inflate(layoutInflater)
}

private var mediaRecorder: MediaRecorder? = null
private var isRecording = false
private var audioFilePath: String? = null
private var isClickType = 1
private var isPlayingVoice = false // Track the playback state
private var lastPosition: Int = 0 // Variable to hold the last playback position
private val PERMISSIONS_REQUEST_CODE = 200
var mediaPlayer: MediaPlayer? = null
private var isPrepared = false
lateinit var mAdapter: VoiceHistoryAdapter
lateinit var mTextAdapter: TextHistoryAdapter
private lateinit var isVoiceHistoryData: List<VoiceHistoryData>
private lateinit var isTextHistoryData: List<TextHistoryData>
private val MAX_RECORDING_TIME = 180

private val handler = Handler(Looper.getMainLooper())
private val progressUpdater = object : Runnable {
override fun run() {
if (isPrepared && mediaPlayer!!.isPlaying) {
val progress = mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration
binding.waveformSeekBar.progress = progress // Ensure this updates correctly
handler.postDelayed(this, 100) // Update every 100ms
}
}
}
private var recordingTime = 0 // Recording time in seconds
private lateinit var recordingHandler: Handler
private lateinit var recordingRunnable: Runnable


@SuppressLint("ClickableViewAccessibility")
override fun setupViews() {
super.setupViews()
setupToolbar()

// Underline text for labels
binding.lblHistoryList.paintFlags =
binding.lblHistoryList.paintFlags or Paint.UNDERLINE_TEXT_FLAG
binding.lblBackToVoiceMessage.paintFlags =
binding.lblBackToVoiceMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG

// Set up listeners for UI elements
binding.rlaVoiceMessage.setOnClickListener(this)
binding.rlaScheduleCall.setOnClickListener(this)
binding.rlaTextMessage.setOnClickListener(this)
binding.rlaFromTime.setOnClickListener(this)
binding.rlaToTime.setOnClickListener(this)
binding.imgVoicePlay.setOnClickListener(this)
binding.imgVoiceRecord.setOnClickListener(this)
binding.lnrHistoryList.setOnClickListener(this)
binding.rlaBackRecord.setOnClickListener(this)
binding.imgBack.setOnClickListener(this)

checkAndRequestPermissions()
audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord.3gp"

val customSwitch: CustomSwitch = findViewById(R.id.SwitchEmergencyVoice)
customSwitch.setOnClickListener {
val state = if (customSwitch.isChecked()) "ON" else "OFF"
Toast.makeText(this, "Switch is $state", Toast.LENGTH_SHORT).show()
}


binding.waveformSeekBar.apply {
progress = 0f // Start with zero progress
waveProgressColor = ContextCompat.getColor(this@CommunicationSchool, R.color.light_green_bg1)
sample = getDummyWaveSample() // Initial setup
}

binding.waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
override fun onProgressChanged(
waveformSeekBar: WaveformSeekBar,
progress: Float,
fromUser: Boolean
) {
if (fromUser && isPrepared) {
val seekPosition = (mediaPlayer!!.duration * progress).toInt()
mediaPlayer!!.seekTo(seekPosition)
}
binding.waveformSeekBar.invalidate() // Force redraw for user-driven updates
}
}

// Initialize handler for updating recording time
recordingHandler = Handler()
recordingRunnable = Runnable {
if (isRecording) {
recordingTime++
binding.lblDurationOfVoice.text = String.format(
"%02d:%02d" + " / 03:00",
recordingTime / 60,
recordingTime % 60
)

if (recordingTime >= MAX_RECORDING_TIME) {
stopRecording()
} else {
recordingHandler.postDelayed(recordingRunnable, 1000) // Update every second
}
}
}
}


private fun startRecording() {
if (checkAndRequestPermissions()) {
mediaRecorder = MediaRecorder().apply {
setAudioSource(MediaRecorder.AudioSource.MIC)
setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
setOutputFile(audioFilePath)
setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

try {
binding.imgVoiceRecord.setImageDrawable(
ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_voice)
)
prepare()
start()
isRecording = true
recordingTime = 0 // Reset recording time
recordingHandler.post(recordingRunnable) // Start updating time
Toast.makeText(
this@CommunicationSchool,
"Recording started",
Toast.LENGTH_SHORT
).show()
} catch (e: IOException) {
e.printStackTrace()
Toast.makeText(this@CommunicationSchool, "Recording failed", Toast.LENGTH_SHORT)
.show()
}
}
} else {
checkAndRequestPermissions()
}
}

private fun stopRecording() {
val parts = binding.lblDurationOfVoice.text.toString().split(" / ")
if (parts.isNotEmpty()) {
val currentDuration = parts[0] // This will be "06:45"
binding.lblEndDuration.text = currentDuration
}
loadWaveform()

mediaRecorder?.apply {
binding.imgVoiceRecord.setImageDrawable(
ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_icon)
)
stop()
release()
mediaRecorder = null
isRecording = false
recordingHandler.removeCallbacks(recordingRunnable) // Stop updating time
Toast.makeText(this@CommunicationSchool, "Recording stopped", Toast.LENGTH_SHORT).show()
Log.d("RecordingFilePath", "Recording stopped. File Path: $audioFilePath") // Print the file path when recording stops
binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
}
}

private fun loadWaveform() {
Thread {
val waveExtractor = WaveExtractor()
val waveformSamples =
waveExtractor.extractWaveHeights(audioFilePath!!, 50) // Extract wave data
runOnUiThread {
binding.waveformSeekBar.sample =
waveformSamples.map { it.toInt() }.toIntArray() // Set waveform samples
binding.waveformSeekBar.invalidate() // Force redraw
}
}.start()
}

private fun initializeMediaPlayer() {
if (audioFilePath.isNullOrEmpty()) {
Log.e("MediaPlayerError", "Audio file path is null or empty")
return
}

mediaPlayer = MediaPlayer().apply {
setDataSource(audioFilePath)
prepareAsync() // Prepare asynchronously

setOnPreparedListener {
isPrepared = true
binding.waveformSeekBar.maxProgress = 1f // Set the max for normalized progress
binding.waveformSeekBar.progress = 0f // Reset progress to 0
startAudioProgressUpdate() // Start updating progress
start() // Start playback
updateCurrentTime() // Start updating current time
}

setOnCompletionListener {
stopAudioProgressUpdate()
lastPosition = 0 // Reset last position on completion
binding.waveformSeekBar.progress = 0f // Reset seek bar on completion
isPlayingVoice = false // Update playback state
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.play_icon_voice)
) // Change icon to play
binding.lblStartDuration.text = "00:00"
}
}
}


private fun updateCurrentTime() {
handler.postDelayed(object : Runnable {
override fun run() {
mediaPlayer?.let { player ->
if (player.isPlaying) {
val currentPosition = player.currentPosition
binding.lblStartDuration.text = formatDuration(currentPosition)
handler.postDelayed(this, 1000) // Schedule the next update
}
}
}
}, 1000)
}

private fun formatDuration(durationInMillis: Int): String {
val minutes = (durationInMillis / 1000) / 60
val seconds = (durationInMillis / 1000) % 60
return String.format("%02d:%02d", minutes, seconds)
}

private fun startAudioProgressUpdate() {
handler.post(progressUpdater)
binding.waveformSeekBar.invalidate() // Force redraw
}

private fun stopAudioProgressUpdate() {
handler.removeCallbacks(progressUpdater)
binding.waveformSeekBar.progress = 0f // Reset progress
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(this, R.drawable.play_icon_voice)
)
}

private fun getDummyWaveSample(): IntArray {
return IntArray(50) { Random().nextInt(50) }
}

override fun onResume() {
super.onResume()
// Consider restoring playback or UI state if necessary
}

override fun onPause() {
super.onPause()
mediaPlayer?.let { player ->
if (player.isPlaying) {
lastPosition = player.currentPosition // Save the current position
player.pause() // Pause playback
isPlayingVoice = false
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(this, R.drawable.play_icon_voice)
)
}
}
}

override fun onDestroy() {
super.onDestroy()
mediaRecorder?.release()
mediaPlayer?.release() // Safe release to avoid NullPointerException
recordingHandler.removeCallbacks(recordingRunnable)
stopAudioProgressUpdate()
}


override fun onClick(p0: View?) {
when (p0?.id) {
R.id.rlaVoiceMessage -> {
isClickType = 1

binding.lblBackToVoiceMessage.text = "Back to voice message"

binding.rlaBackRecord.visibility = View.GONE
binding.lnrHistoryList.visibility = View.VISIBLE
binding.rlaScheduleCallPickDate.visibility = View.GONE
binding.rlaMessageFromText.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.VISIBLE
binding.rcyHistoryDataVoiceAndText.visibility = View.GONE

isChangeBackRoundCommunicationType(
binding.rlaVoiceMessage,
binding.imgVoiceMessage,
binding.lblVoiceMessage
)
}
R.id.rlaScheduleCall -> {
isClickType = 2
binding.lblBackToVoiceMessage.text = "Back to voice message"

binding.rlaBackRecord.visibility = View.GONE
binding.lnrHistoryList.visibility = View.VISIBLE
binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
binding.rlaMessageFromText.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.VISIBLE
binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
isChangeBackRoundCommunicationType(
binding.rlaScheduleCall,
binding.imgScheduleCall,
binding.lblScheduleCall
)
}
R.id.rlaTextMessage -> {
isClickType = 3
binding.lblBackToVoiceMessage.text = "Back to text message"

binding.rlaBackRecord.visibility = View.GONE
binding.lnrHistoryList.visibility = View.VISIBLE
binding.rlaMessageFromText.visibility = View.VISIBLE
binding.rlaScheduleCallPickDate.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.GONE
binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
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

if (isPlayingVoice) {
// Pause the media player
mediaPlayer?.let {
it.pause()
lastPosition = it.currentPosition // Save current position
isPlayingVoice = false
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.play_icon_voice
) // Change icon to play
)
}
} else {
// If the media player is not initialized, initialize it
if (!isPrepared) {
initializeMediaPlayer() // Prepare the media player for the first time
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.pause_icon
) // Change icon to pause
)
isPlayingVoice = true
} else {
// Resume playback from the last position
mediaPlayer?.let {
it.seekTo(lastPosition) // Seek to last position
it.start() // Start playing
isPlayingVoice = true
binding.imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.pause_icon
) // Change icon to pause
)
startAudioProgressUpdate() // Start updating progress again
}
}
}
}

R.id.imgVoiceRecord -> {
if (!isRecording) {
startRecording()
} else {
stopRecording()
}
}

R.id.imgBack -> {
onBackPressed()
}

R.id.rlaBackRecord -> {

Log.d("isClickType", isClickType.toString())
when (isClickType) {
1 -> {
binding.rlaScheduleCallPickDate.visibility = View.GONE
}

2 -> {
binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
}

else -> {
binding.rlaScheduleCallPickDate.visibility = View.GONE
}
}

binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
binding.rlaBackRecord.visibility = View.GONE
binding.lnrHistoryList.visibility = View.VISIBLE
binding.gridViewScheduleCall.visibility = View.VISIBLE
binding.rlaRecordVoice.visibility = View.VISIBLE

}

R.id.lnrHistoryList -> {
when (isClickType) {
1 -> {
binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
binding.lnrHistoryList.visibility = View.GONE
binding.rlaBackRecord.visibility = View.VISIBLE
binding.gridViewScheduleCall.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.GONE
binding.rlaMessageFromText.visibility = View.GONE
loadData()
}

2 -> {
binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
binding.lnrHistoryList.visibility = View.GONE
binding.rlaBackRecord.visibility = View.VISIBLE
binding.gridViewScheduleCall.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.GONE
binding.rlaMessageFromText.visibility = View.GONE
loadData()
}

else -> {
binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
binding.lnrHistoryList.visibility = View.GONE
binding.rlaBackRecord.visibility = View.VISIBLE
binding.gridViewScheduleCall.visibility = View.GONE
binding.rlaRecordVoice.visibility = View.GONE
binding.rlaMessageFromText.visibility = View.GONE
loadTextData()
}
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

binding.imgVoiceMessage.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.mic_icon_black
)
)

binding.imgScheduleCall.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.call_schedule_icon_black
)
)

binding.imgTextMessage.setImageDrawable(
ContextCompat.getDrawable(
this,
R.drawable.text_icon_black
)
)

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


private fun loadData() {
isVoiceHistoryData = listOf(
VoiceHistoryData(
"Annual Day celebrations",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
),
VoiceHistoryData(
"Parent Meeting",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
),
VoiceHistoryData(
"Normal Day",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
),
VoiceHistoryData(
"Day",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3"
),
VoiceHistoryData(
"Monday",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_1MG.mp3"
),
VoiceHistoryData(
"Nothing",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav"
),
VoiceHistoryData(
"Value Education",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
),
VoiceHistoryData(
"Environmental Science",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://filesamples.com/samples/audio/mp3/sample4.mp3"
),
VoiceHistoryData(
"Okay okay",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"https://ia800304.us.archive.org/8/items/testmp3testfile/mpthreetest.mp3"
)
)

mAdapter = VoiceHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false;
binding.rcyHistoryDataVoiceAndText.adapter = mAdapter

Constant.executeAfterDelay {
// Once data is loaded, stop shimmer and pass the actual data
mAdapter =
VoiceHistoryAdapter(isVoiceHistoryData, this, this, Constant.isShimmerViewDisable)
// Set GridLayoutManager (2 columns in this case)
binding.rcyHistoryDataVoiceAndText.adapter = mAdapter
}
}

private fun loadTextData() {
isTextHistoryData = listOf(
TextHistoryData(
"Annual Day celebrations",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Parent Meeting",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Normal Day",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Day",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Monday",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Nothing",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Value Education",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Environmental Science",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
),
TextHistoryData(
"Okay okay",
"If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
"Apr 1, 2021"
)
)

mTextAdapter = TextHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false;
binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter

Constant.executeAfterDelay {
// Once data is loaded, stop shimmer and pass the actual data
mTextAdapter =
TextHistoryAdapter(isTextHistoryData, this, this, Constant.isShimmerViewDisable)
// Set GridLayoutManager (2 columns in this case)
binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter
}
}

private fun checkAndRequestPermissions(): Boolean {
val requiredPermissions = mutableListOf<String>()

if (ContextCompat.checkSelfPermission(
this,
Manifest.permission.RECORD_AUDIO
) != PackageManager.PERMISSION_GRANTED
) {
requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
}

if (ContextCompat.checkSelfPermission(
this,
Manifest.permission.READ_EXTERNAL_STORAGE
) != PackageManager.PERMISSION_GRANTED
) {
requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
}

if (ContextCompat.checkSelfPermission(
this,
Manifest.permission.WRITE_EXTERNAL_STORAGE
) != PackageManager.PERMISSION_GRANTED
) {
requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

return if (requiredPermissions.isNotEmpty()) {
ActivityCompat.requestPermissions(
this,
requiredPermissions.toTypedArray(),
PERMISSIONS_REQUEST_CODE
)
false
} else {
true
}
}

override fun onRequestPermissionsResult(
requestCode: Int,
permissions: Array<out String>,
grantResults: IntArray
) {
super.onRequestPermissionsResult(requestCode, permissions, grantResults)
if (requestCode == PERMISSIONS_REQUEST_CODE) {
val deniedPermissions = permissions.zip(grantResults.toTypedArray())
.filter { it.second != PackageManager.PERMISSION_GRANTED }
.map { it.first }

if (deniedPermissions.isNotEmpty()) {
// Inform the user that permissions were denied and are necessary.
Toast.makeText(
this,
"Required permissions denied: $deniedPermissions",
Toast.LENGTH_LONG
).show()
}
}
}

override fun onItemClick(data: VoiceHistoryData, holder: VoiceHistoryAdapter.DataViewHolder) {

}

override fun onItemClick(data: TextHistoryData, holder: TextHistoryAdapter.DataViewHolder) {

}
}
 */

