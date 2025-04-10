package com.vs.schoolmessenger.School.Communication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomDatePicker
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.IOException
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
    var mediaPlayer: MediaPlayer? = null
    private val REQUEST_PERMISSIONS = 123
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    private var isPrepared = false
    var mAdapter: VoiceHistoryAdapter? = null
    var mTextAdapter: TextHistoryAdapter? = null
    private lateinit var isVoiceHistoryData: List<VoiceHistoryDetails>
    private lateinit var isTextHistoryData: List<TextHistoryData>
    private val MAX_RECORDING_TIME = 180
    private val handler = Handler(Looper.getMainLooper())

    var isEmergency = 0
    var isScheduleCall = false

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer!!.isPlaying) {
//                val progress = mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration
//                binding.waveformSeekBar.progress = progress // Ensure this updates correctly
                handler.postDelayed(this, 100) // Update every 100ms
            }
        }
    }
    private val selectedDates = ArrayList<String>()
    private var recordingTime = 0
    private lateinit var recordingHandler: Handler
    private lateinit var recordingRunnable: Runnable
    var isMultipleSchool = false
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isUserDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null

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
        binding.rlaSendVoice.setOnClickListener(this)
        binding.rlaSendText.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token


        checkAndRequestPermissions(this)
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord.m4a"
        mediaRecorder!!.setOutputFile(audioFilePath)

        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        isUserDetails = SharedPreference.getUserDetails(this)

        if (isUserDetails!!.staff_details.size > 1) {
            isMultipleSchool = true
        } else {
            isMultipleSchool = false
        }

        if (isUserDetails!!.staff_role == Constant.isGroupHeadRole || isUserDetails!!.staff_role == Constant.isPrincipalRole || isUserDetails!!.staff_role == Constant.isAdminRole) {
            binding.SwitchEmergencyVoice.visibility = View.VISIBLE
            binding.lblEmergencyVoice.visibility = View.VISIBLE
        } else {
            binding.SwitchEmergencyVoice.visibility = View.GONE
            binding.lblEmergencyVoice.visibility = View.GONE
        }

        binding.lblStartTime.text = Constant.getCurrentTime()
        binding.lblEndTime.text = Constant.getCurrentTime()

        appViewModel!!.isGetVoiceHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val isGetHistory = response.data
                isVoiceHistoryData = isGetHistory
                loadVoiceData(isVoiceHistoryData)
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }

        changeLabel()
        binding.SwitchEmergencyVoice.setOnClickListener {
            if (binding.SwitchEmergencyVoice.isChecked()) {
                Constant.isEmergencyVoiceNoticeBoard = true
                Constant.isAccessType = Constant.isEmergency
                isEmergency = 1
            } else {
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
            }
            changeLabel()
        }

        // Initialize handler for updating recording time
        recordingHandler = Handler()
        recordingRunnable = Runnable {
            if (isRecording) {
                recordingTime++
                binding.lblDurationOfVoice.text = String.format(
                    "%02d:%02d" + " / 03:00", recordingTime / 60, recordingTime % 60
                )

                if (recordingTime >= MAX_RECORDING_TIME) {
                    stopRecording()
                } else {
                    recordingHandler.postDelayed(recordingRunnable, 1000) // Update every second
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSIONS) {
            var permanentlyDenied = false
            var allGranted = true

            permissions.forEachIndexed { index, perm ->
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                        permanentlyDenied = true
                    }
                }
            }

            when {
                allGranted -> {
                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
                }

                permanentlyDenied -> {
                    Toast.makeText(
                        this,
                        "Permissions permanently denied. Go to settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    openAppSettings()
                }

                else -> {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun checkAndRequestPermissions(activity: Activity): Boolean {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        permissions.add(Manifest.permission.RECORD_AUDIO)

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (deniedPermissions.isEmpty()) {
            true
        } else {
            // Request the denied permissions
            ActivityCompat.requestPermissions(activity, deniedPermissions.toTypedArray(), 100)
            false
        }
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }


    private fun changeLabel() {
        if (Constant.isEmergencyVoiceNoticeBoard == true) {
            if (isMultipleSchool) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                if (isEmergency == 0) {
                    binding.lblSend.text = resources.getString(R.string.NEXT)
                } else {
                    binding.lblSend.text = resources.getString(R.string.Send)
                }
            }
        } else {
            if (isEmergency == 0) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                binding.lblSend.text = resources.getString(R.string.Send)
            }
        }
    }

    private fun startRecording() {
        if (checkAndRequestPermissions(this)) {
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
                        this@CommunicationSchool, "Recording started", Toast.LENGTH_SHORT
                    ).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@CommunicationSchool, "Recording failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            checkAndRequestPermissions(this)
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
                "RecordingFilePath", "Recording stopped. File Path: $audioFilePath"
            ) // Print the file path when recording stops
            Constant.isVoiceFile = audioFilePath
            binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
            binding.rlaTitle.visibility = View.VISIBLE
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
        if (checkAndRequestPermissions(this)) {
            println("Permissions granted after returning from settings.") // Debug log
        } else {
            openAppSettings()
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

    private fun isFileUploadInAws(
        isFilePath: String,
        bucketPath: String,
        isFileExtension: String?,
        filetype: String,
    ) {
        Log.d("isFilePath____", isFilePath)
        isAwsUploadingPreSigned!!.getPreSignedUrl(
            isFilePath,
            bucketPath,
            isFileExtension!!,
            this,
            "1",
            true,
            false,
            object : UploadCallback {
                override fun onUploadSuccess(
                    response: String?,
                    isFileUploaded: String?
                ) {
                    //isVoiceSend(isFileUploaded)
                }

                override fun onUploadError(error: String?) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()
                binding.SwitchEmergencyVoice.visibility = View.VISIBLE
                binding.lblEmergencyVoice.visibility = View.VISIBLE
                isScheduleCall = false
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
                binding.rlaSendText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE

                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage, binding.imgVoiceMessage, binding.lblVoiceMessage
                )
            }

            R.id.rlaScheduleCall -> {
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()
                binding.SwitchEmergencyVoice.visibility = View.GONE
                binding.lblEmergencyVoice.visibility = View.GONE
                isScheduleCall = true
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
                binding.rlaSendText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall, binding.imgScheduleCall, binding.lblScheduleCall
                )
            }

            R.id.rlaTextMessage -> {
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()

                binding.SwitchEmergencyVoice.visibility = View.GONE
                binding.lblEmergencyVoice.visibility = View.GONE
                isScheduleCall = false
                isClickType = 3
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "Back to text message"
                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.rlaMessageFromText.visibility = View.VISIBLE
                binding.rlaSendText.visibility = View.VISIBLE
                binding.rlaScheduleCallPickDate.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.GONE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage, binding.imgTextMessage, binding.lblTextMessage
                )
            }

            R.id.rlaFromTime -> {
                showTimePickerDialog(this, this)
            }
            R.id.rlaSendText -> {
                if (binding.edtTitleTextMessage.text.toString() != "") {
                    if (binding.edtContentTextMessage.text.toString() != "") {
                        isGoToRecipient()
                    } else {
                        Constant.showAlert("Alert", "Enter the Content", this)
                    }
                } else {
                    Constant.showAlert("Alert", "Enter the title", this)
                }
            }

            R.id.rlaToTime -> {
                showTimePickerDialog(this, this)
            }

            R.id.rlaSendVoice -> {
                if (!Constant.isVoiceFile.equals("")) {
                    if (binding.edtTitle.text.toString() != "") {
                        if (binding.lblSend.text.toString() == resources.getString(R.string.send)) {
//                        isFileUploadInAws(Constant.isVoiceFile!!, isStaffDetails!!.school_id, "audio", ".mp3")
                            showSendConfirmationDialog("Are you want send this voice to entire school?")
                        } else {
                            isGoToRecipient()
                        }
                    } else {
                        Constant.showAlert("Alert!", "Enter the title", this)
                    }
                } else {
                    Constant.showAlert("Alert!", "Record or pick the voice file!", this)
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
                                this, R.drawable.video_play
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
                                this, R.drawable.pause_icon
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
                                    this, R.drawable.pause_icon
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

                val dateAdapter = DateAdapter(this) { updatedList -> }
                selectedDatesAdapter = SelectedDatesAdapter(
                    this,
                    selectedDates.toMutableList(),
                    dateAdapter
                ) { removedDate ->
                    dateAdapter.removeSelectedDate(removedDate)
                }
                binding.gridViewScheduleCall.adapter = selectedDatesAdapter
                val datePickerPopup = CustomDatePicker(
                    context = this,
                    preSelectedDates = selectedDates.toList(),
                    dateAdapter = dateAdapter
                ) { newSelectedDates ->
                    selectedDates.clear()
                    selectedDates.addAll(newSelectedDates)
                    selectedDatesAdapter.submitSelectedDates(selectedDates.toList())
                }
                datePickerPopup.show(window.decorView.rootView)
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
                        binding.rlaSendText.visibility = View.VISIBLE
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
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        isGetVoiceHistory()
                    }

                    2 -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        binding.gridViewScheduleCall.visibility = View.VISIBLE
                        isGetVoiceHistory()
                    }

                    else -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        //   loadTextData()
                    }
                }
            }
        }
    }

    private fun isGoToRecipient() {
        val isStaffRole = isUserDetails!!.staff_role
        if (isMultipleSchool) {
            if (isStaffRole.equals(Constant.isGroupHeadRole) || isStaffRole.equals(
                    Constant.isPrincipalRole
                ) || isStaffRole.equals(
                    Constant.isAdminRole
                )
            ) {
                val intent = Intent(this, SchoolList::class.java)
                isSaveTheVoiceData()
                startActivity(intent)
            } else {
                val intent = Intent(this, RecipientActivity::class.java)
                isSaveTheVoiceData()
                startActivity(intent)
            }
        } else {
            if (isStaffRole.equals(Constant.isGroupHeadRole) || isStaffRole.equals(
                    Constant.isPrincipalRole
                ) || isStaffRole.equals(
                    Constant.isAdminRole
                )
            ) {
                if (Constant.isEmergencyVoiceNoticeBoard == true) {
                    //send api call here itself
                } else {
                    val intent = Intent(this, RecipientActivity::class.java)
                    isSaveTheVoiceData()
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, RecipientActivity::class.java)
                isSaveTheVoiceData()
                startActivity(intent)
            }
        }
    }

    fun isSaveTheVoiceData() {
        val voiceData = VoiceSendingData(
            isFileUploaded = "isVoiceUrl",
            isClickType = isClickType,
            selectedDates = selectedDates,
            isStartTimeText = binding.lblStartTime.text.toString(),
            isEndTimeText = binding.lblEndTime.text.toString(),
            title = binding.edtTitle.text.toString(),
            isEmergency = isEmergency,
            isScheduleCall = isScheduleCall
        )
        Constant.isVoiceSendingData = voiceData
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
                this, R.drawable.mic_icon_black
            )
        )

        binding.imgScheduleCall.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.call_schedule_icon_black
            )
        )

        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.text_icon_black
            )
        )

        // Update icons based on selection
        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> {
                binding.imgVoiceMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.mic_icon
                    )
                )
            }

            binding.imgScheduleCall -> {
                binding.imgScheduleCall.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.call_schedule_icon
                    )
                )
            }

            binding.imgTextMessage -> {
                binding.imgTextMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this, R.drawable.text_icon
                    )
                )
            }
        }
    }

    private fun isGetVoiceHistory() {
        appViewModel!!.isGetVoiceHistory(isAccessToken!!, "0", this)
    }

    private fun loadVoiceData(isVoiceHistoryData: List<VoiceHistoryDetails>) {
        Log.d("isVoiceHistory", "isVoiceHistory")
        mAdapter = VoiceHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false
        binding.rcyHistoryDataVoiceAndText.adapter = mAdapter

        Constant.executeAfterDelay {
            mAdapter =
                VoiceHistoryAdapter(isVoiceHistoryData, this, this, Constant.isShimmerViewDisable)
            binding.rcyHistoryDataVoiceAndText.adapter = mAdapter
        }
    }

    private fun loadTextData() {
        isTextHistoryData = listOf(
            TextHistoryData(
                "Annual Day celebrations",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Parent Meeting",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Normal Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Monday",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Nothing",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Value Education",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
                "Environmental Science",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ), TextHistoryData(
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

    override fun onItemClick(data: TextHistoryData, holder: TextHistoryAdapter.DataViewHolder) {

    }

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblStartTime.text = String.format("%02d:%02d %s", hour, minute, amPm)
        binding.lblEndTime.text = String.format("%02d:%02d %s", hour, minute, amPm)

    }

    override fun onItemClick(
        data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder
    ) {

    }

    fun showSendConfirmationDialog(isMessage: String) {
        val isSchoolId = mutableListOf(isStaffDetails!!.school_id.toInt())
        AlertDialog.Builder(this).setTitle("Send Confirmation!").setMessage(isMessage)
            .setPositiveButton("Yes") { dialog, _ ->
                val isVoiceUrl =
                    "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/2025-04-09/5512/audiorecord.m4a"
                val jsonObject = ApiCallRequest.isVoiceSend(
                    isFileUploaded = isVoiceUrl,
                    isClickType = isClickType,
                    selectedDates = selectedDates,
                    isStartTimeText = binding.lblStartTime.text.toString(),
                    isEndTimeText = binding.lblEndTime.text.toString(),
                    title = binding.edtTitle.text.toString(),
                    isEmergency = isEmergency,
                    isScheduleCall = isScheduleCall,
                    schoolId = isSchoolId,
                    targetType = Constant.isSchool,
                    circularType = Constant.school
                )
                appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}