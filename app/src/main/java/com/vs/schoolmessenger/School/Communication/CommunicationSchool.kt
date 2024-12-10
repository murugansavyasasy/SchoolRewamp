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
    VoiceHistoryClickListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null


    private var isPlayingVoice = false // Track the playback state
//    private val audioUrl = "http://vs5.voicesnapforschools.com/nodejs/voice/VS_1718181818812.wav"
    private var lastPosition: Int = 0 // Variable to hold the last playback position
    private val PERMISSIONS_REQUEST_CODE = 200

    private lateinit var mediaPlayer: MediaPlayer
    private var isPrepared = false
    lateinit var mAdapter: VoiceHistoryAdapter
    private lateinit var isVoiceHistoryData: List<VoiceHistoryData>
    private val MAX_RECORDING_TIME = 180

    private val handler = Handler(Looper.getMainLooper())
    private val progressUpdater = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer.isPlaying) {
                val progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
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
        loadWaveform()
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
        binding.lblHistoryList.setOnClickListener(this)
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
                    val seekPosition = (mediaPlayer.duration * progress).toInt()
                    mediaPlayer.seekTo(seekPosition)
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
                        ContextCompat.getDrawable(
                            this@CommunicationSchool,
                            R.drawable.record_voice
                        )
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
        mediaRecorder?.apply {

            binding.imgVoiceRecord.setImageDrawable(
                ContextCompat.getDrawable(
                    this@CommunicationSchool,
                    R.drawable.record_icon
                )
            )

            stop()
            release()
            mediaRecorder = null
            isRecording = false
            recordingHandler.removeCallbacks(recordingRunnable) // Stop updating time
            Toast.makeText(this@CommunicationSchool, "Recording stopped", Toast.LENGTH_SHORT).show()
            Log.d("RecordingFilePath", "Recording stopped. File Path: $audioFilePath") // Print the file path when recording stops
            binding.rlaSeekBarAndTitle.visibility=View.VISIBLE

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
            }
        }.start()
    }


    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepareAsync() // Prepare asynchronously
            setOnPreparedListener {
                isPrepared = true
                binding.waveformSeekBar.maxProgress = 1f // Set the max for normalized progress
                binding.waveformSeekBar.progress = 0f // Reset progress to 0
                loadWaveform() // Load waveform samples
                startAudioProgressUpdate() // Start updating progress
                start() // Start playback
            }
            setOnCompletionListener {
                stopAudioProgressUpdate()
                lastPosition = 0 // Reset last position on completion
                binding.waveformSeekBar.progress = 0f // Reset seek bar on completion
                isPlayingVoice = false // Update playback state
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@CommunicationSchool,
                        R.drawable.play_icon_voice
                    )
                ) // Change icon to play
            }
        }
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
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            lastPosition = mediaPlayer.currentPosition // Save the current position
            mediaPlayer.pause() // Pause playback
            isPlayingVoice = false
            binding.imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.play_icon_voice
                )
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer.release()
        recordingHandler.removeCallbacks(recordingRunnable)
        stopAudioProgressUpdate()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                binding.rlaScheduleCallPickDate.visibility=View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }
            R.id.rlaScheduleCall -> {
                binding.rlaScheduleCallPickDate.visibility=View.VISIBLE
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall,
                    binding.imgScheduleCall,
                    binding.lblScheduleCall
                )
            }
            R.id.rlaTextMessage -> {
                binding.rlaScheduleCallPickDate.visibility=View.GONE
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
                    mediaPlayer.pause()
                    lastPosition = mediaPlayer.currentPosition // Save current position
                    isPlayingVoice = false
                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.play_icon_voice
                        )
                    ) // Change icon to play
                } else {
                    // If the media player is not initialized, initialize it
                    if (!isPrepared) {
                        binding.imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        ) // Change icon to pause
                        initializeMediaPlayer() // Prepare the media player for the first time
                    } else {
                        mediaPlayer.seekTo(lastPosition) // Seek to last position
                        mediaPlayer.start() // Start playing
                        isPlayingVoice = true
                        binding.imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        ) // Change icon to pause
                        startAudioProgressUpdate() // Start updating progress again
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

                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                binding.rlaScheduleCallPickDate.visibility = View.GONE
                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.VISIBLE
                binding.rlaRecordVoice.visibility = View.VISIBLE

            }

            R.id.lblHistoryList -> {
                binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                binding.rlaBackRecord.visibility = View.VISIBLE
                binding.gridViewScheduleCall.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.GONE
                loadData()
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
}