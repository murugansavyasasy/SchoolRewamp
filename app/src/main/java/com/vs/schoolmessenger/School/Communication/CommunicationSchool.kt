package com.vs.schoolmessenger.School.Communication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Paint
import android.media.MediaPlayer
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
import java.util.Random

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener,
    VoiceHistoryClickListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private var isPlayingVoice = false // Track the playback state
    private val audioUrl = "http://vs5.voicesnapforschools.com/nodejs/voice/VS_1718181818812.wav"
    private var lastPosition: Int = 0 // Variable to hold the last playback position
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var mediaPlayer: MediaPlayer
    private var isPrepared = false

    lateinit var mAdapter: VoiceHistoryAdapter
    private lateinit var isVoiceHistoryData: List<VoiceHistoryData>

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

        val customSwitch: CustomSwitch = findViewById(R.id.SwitchEmergencyVoice)
        customSwitch.setOnClickListener {
            val state = if (customSwitch.isChecked()) "ON" else "OFF"
            Log.d("CommunicationSchool", "Switch state: $state")
            Toast.makeText(this, "Switch is $state", Toast.LENGTH_SHORT).show()
        }

        checkPermissions()

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
    }

    private fun loadWaveform() {
        Thread {
            val waveExtractor = WaveExtractor()
            val waveformSamples =
                waveExtractor.extractWaveHeights(audioUrl, 50) // Extract wave data
            runOnUiThread {
                binding.waveformSeekBar.sample =
                    waveformSamples.map { it.toInt() }.toIntArray() // Set waveform samples
            }
        }.start()
    }


    private fun initializeMediaPlayer(audioUrl: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
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


    private fun checkPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    private fun hasPermissions(): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getDummyWaveSample(): IntArray {
        return IntArray(50) { Random().nextInt(50) }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.let {
            if (it.isPlaying) {
                lastPosition = it.currentPosition
                it.pause()
                isPlayingVoice = false
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.play_icon_voice)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        stopAudioProgressUpdate()
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
                isPlayingVoice(audioUrl)
            }


            R.id.imgVoiceRecord -> {

            }

            R.id.lblHistoryList -> {
                binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.GONE
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
        binding.rcyHistoryDataVoiceAndText.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                VoiceHistoryAdapter(isVoiceHistoryData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyHistoryDataVoiceAndText.adapter = mAdapter
        }
    }

    override fun onItemClick(data: VoiceHistoryData, holder: VoiceHistoryAdapter.DataViewHolder) {
        isPlayingVoice(data.url)
    }


    private fun isPlayingVoice(audioUrl: String) {
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
                initializeMediaPlayer(audioUrl) // Prepare the media player for the first time
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
}