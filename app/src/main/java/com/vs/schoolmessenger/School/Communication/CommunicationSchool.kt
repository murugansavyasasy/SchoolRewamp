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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomDatePicker
import com.vs.schoolmessenger.Utils.FileExtensionFromContentUri
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener,
    VoiceHistoryClickListener, TextHistoryClickListener, TimeSelectedListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private var isInitialized = false
    private lateinit var selectedDatesAdapter: SelectedDatesAdapter
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null
    private var isPlayingVoice = false
    private var lastPosition: Int = 0
    var mediaPlayer: MediaPlayer? = null
    private val REQUEST_PERMISSIONS = 100
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isFileExtensionFromContentUri: FileExtensionFromContentUri? = null
    private var isPrepared = false
    var mAdapter: VoiceHistoryAdapter? = null
    var mTextAdapter: TextHistoryAdapter? = null
    private lateinit var isVoiceHistoryData: List<VoiceHistoryDetails>
    private lateinit var isTextHistoryData: List<TextDetail>
    private var MAX_RECORDING_TIME = 180
    private val handler = Handler(Looper.getMainLooper())

    private var hasRequestedPermissions = false
    private var returnedFromSettings = false

    var isEmergency = 0
    var isScheduleCall = false
    private val PICK_AUDIO_REQUEST = 101
    var isAcademicYearId = -1
    var isAcademicYear: List<AcademicYear>? = null
    var isFileName: String? = null
    private val progressUpdater = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer!!.isPlaying) {
                handler.postDelayed(this, 100)
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setUpGradientSchool()

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
        binding.lottieAnimationView.setOnClickListener(this)
        binding.lnrHistoryList.setOnClickListener(this)
        binding.rlaBackRecord.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.lnrScheduleCall.setOnClickListener(this)
        binding.rlaSendVoice.setOnClickListener(this)
        binding.rlaSendText.setOnClickListener(this)
        binding.rlaAddLocalFile.setOnClickListener(this)
        binding.rlaAcademicYear.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.infosymbol.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        if (!checkAndRequestPermissions(this)) {
            return
        }
        mediaRecorder = MediaRecorder()

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isFileExtensionFromContentUri = FileExtensionFromContentUri()

        isUserDetails = SharedPreference.getUserDetails(this)

        if (isUserDetails!!.staff_details.size > 1) {
            isMultipleSchool = true
        } else {
            isMultipleSchool = false
        }

        binding.lblStartTime.text = Constant.getCurrentTime()
        binding.lblEndTime.text = Constant.getTimeAfter20Minutes()

        if (isUserDetails!!.staff_role == "p3") {
            binding.rlaScheduleCall.visibility = View.GONE
        } else {
            binding.rlaScheduleCall.visibility = View.VISIBLE
        }

        appViewModel!!.isGetVoiceHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val isGetHistory = response.data
                isVoiceHistoryData = isGetHistory
                loadVoiceData(isVoiceHistoryData)
            }
        }

        appViewModel!!.isGetTextHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val isTextHistory = response.data
                isTextHistoryData = isTextHistory
                loadTextHistoryData(isTextHistoryData)
            }
        }

        changeLabel()
        binding.SwitchEmergencyVoice.setOnClickListener {
            if (binding.SwitchEmergencyVoice.isChecked()) {
                Constant.isAccessType = Constant.isEmergency
                binding.lblDurationOfVoice.text = "00:00 / 00:30"
                isEmergency = 1
                MAX_RECORDING_TIME = 30
                val popupWindow = infosymbolload()

                Handler(Looper.getMainLooper()).postDelayed({
                    popupWindow.dismiss() // Dismiss the tooltip after 2 seconds
                }, 2000)
            } else {

                Constant.isAccessType = Constant.isNonEmergency
                binding.lblDurationOfVoice.text = "00:00 / 03:00"
                isEmergency = 0
                MAX_RECORDING_TIME = 180
            }
            changeLabel()
        }

        // Initialize handler for updating recording time
        recordingHandler = Handler()
        recordingRunnable = Runnable {
            if (isRecording) {
                recordingTime++
                binding.lblDurationOfVoice.text = String.format(
                    "%02d:%02d / %s",
                    recordingTime / 60,
                    recordingTime % 60,
                    if (MAX_RECORDING_TIME == 30) "00:30" else "03:00"
                )

                if (recordingTime >= MAX_RECORDING_TIME) {
                    stopRecording()
                } else {
                    recordingHandler.postDelayed(recordingRunnable, 1000)
                }
            }
        }
        val isCurrentTime = Constant.getCurrentTime()
        binding.lblTime.text = isCurrentTime

        binding.edtContentTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.lblCountOfDescription.text = "$length/500"
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.edtTitleTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.lblCountOfTitle.text = "$length/50"
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.edtTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.lblCountOfTitleVoice.text = "$length/50"
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun loadTextHistoryData(isTextHistoryDetails: List<TextDetail>) {
            mTextAdapter =
                TextHistoryAdapter(isTextHistoryDetails, this, this, Constant.isShimmerViewDisable)
            binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter
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
                    // All permissions granted
                    hasRequestedPermissions = false
                }

                permanentlyDenied -> {
                    showPermissionSettingsDialog()
                }

                else -> {
                    hasRequestedPermissions = false
                    checkAndRequestPermissions(this)
                }
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Some permissions are permanently denied. Please enable them in app settings to proceed.")
            .setPositiveButton("OK") { _, _ ->
                openAppSettings()
            }
            .setCancelable(false)
            .show()
    }



    private fun changeLabel() {
        binding.lblSend.text = resources.getString(R.string.NEXT)

        if (Constant.isEmergencyVoiceNoticeBoard == true) {
            if (isMultipleSchool) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                if (isEmergency == 0) {
                    binding.lblSend.text = resources.getString(R.string.NEXT)
                } else {
                    binding.lblSend.text = resources.getString(R.string.NEXT)
                }
            }
        } else {
            if (isEmergency == 0) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            }
        }
    }
    private fun startRecording() {
        if (checkAndRequestPermissions(this)) {

            val dir = externalCacheDir ?: cacheDir
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Communication_$timeStamp.mp3"
            val filePath = "${dir.absolutePath}/$fileName"

            audioFilePath = filePath
            isFileName = fileName // <-- Store if needed elsewhere
            Constant.isVoiceType = 1
            Constant.isVoiceFile = audioFilePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)

                try {
//                    binding.imgVoiceRecord.setImageDrawable(
//                        ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_voice)
//                    )
                    binding.lottieAnimationView.visibility = View.VISIBLE
                    binding.imgVoiceRecord.visibility = View.GONE
                    binding.lottieAnimationView.setAnimation(R.raw.voice_record)
                    binding.lottieAnimationView.loop(true)
                    binding.lottieAnimationView.playAnimation()

                    prepare()
                    start()
                    isRecording = true
                    recordingTime = 0
                    recordingHandler.post(recordingRunnable)

                    binding.lblDurationOfVoice.visibility = View.VISIBLE
                    binding.lblDurationOfVoice.text = "Recording: $fileName"

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        } else {
            openAppSettings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                mediaRecorder = null
                isRecording = false
                recordingHandler.removeCallbacks(recordingRunnable)

                binding.imgVoiceRecord.visibility = View.VISIBLE
                binding.lottieAnimationView.visibility = View.GONE
                binding.imgVoiceRecord.setImageDrawable(
                    ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_icon)
                )
                binding.rlaAddLocalFile.visibility = View.GONE

                val file = File(audioFilePath)
                Log.d(
                    "RecordingFilePath",
                    "Stopped. Path: $audioFilePath, Exists: ${file.exists()}, Size: ${file.length()} bytes"
                )

                if (file.exists() && file.length() > 0L) {
                    Constant.isVoiceFile = audioFilePath
                    Constant.isVoiceType = 1

                    // Get duration using MediaPlayer
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(audioFilePath)
                    mediaPlayer.prepare()
                    val durationInMs = mediaPlayer.duration
                    mediaPlayer.release()

                    val formattedDuration = formatDuration(durationInMs)
                    binding.lblEndDuration.text = "/ $formattedDuration"

                    binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
                    binding.rlaTitle.visibility = View.VISIBLE

                } else {
                    Toast.makeText(this@CommunicationSchool, "Recording failed", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@CommunicationSchool,
                    "Failed to stop recording",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initializeMediaPlayer() {
        if (audioFilePath.isNullOrEmpty()) {
            Log.e("MediaPlayerError", "Audio file path is null or empty")
            return
        }

        // Release any existing player
        mediaPlayer?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.w("MediaPlayerError", "Tried to stop() a player not in a valid state.")
            }
            release()
        }

        // Extract extension (just for logging)
        val fileExtension = getFileExtension(audioFilePath!!)
        Log.d("MediaPlayerDebug", "File extension: $fileExtension")

        mediaPlayer = MediaPlayer().apply {
            try {
                val uri = Uri.parse(audioFilePath)

                if (audioFilePath!!.startsWith("content://") || audioFilePath!!.startsWith("file://")) {
                    setDataSource(this@CommunicationSchool, uri)
                } else if (audioFilePath!!.startsWith("http")) {
                    setDataSource(audioFilePath)
                } else {
                    // Use file path directly
                    setDataSource(audioFilePath)
                }

                setOnPreparedListener {
                    isPrepared = true
                    val totalDurationInMillis = it.duration
                    val totalFormatted = formatDuration(totalDurationInMillis)

                    it.start()
                    isPlayingVoice = true
                    startAudioProgressUpdate()
                    updateCurrentTime(totalDurationInMillis)

                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.pause_icon)
                    )
                    binding.lblEndDuration.text ="/ " +totalFormatted
                }



                setOnCompletionListener {
                    stopAudioProgressUpdate()
                    lastPosition = 0
                    isPlayingVoice = false
                    isPrepared = false

                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.video_play)
                    )
                    binding.lblStartDuration.text = "00:00"
                    binding.waveformSeekBar.updateWithLevel(0f)
                    Log.d("AudioDebug", "Playback completed.")
                }

                prepareAsync()

            } catch (e: IOException) {
                Log.e("MediaPlayerError", "IO Error: ${e.message}")
            } catch (e: IllegalStateException) {
                Log.e("MediaPlayerError", "Illegal state: ${e.message}")
            } catch (e: Exception) {
                Log.e("MediaPlayerError", "Unexpected error: ${e.message}")
            }
        }
    }


    private fun updateCurrentTime(totalDurationMillis: Int) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        val currentFormatted = formatDuration(currentPosition)
                        Log.d("currentFormatted",currentFormatted.toString())
                        binding.lblStartDuration.text =  currentFormatted
                        handler.postDelayed(this, 1000)
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
            if (!hasRequestedPermissions) {
                hasRequestedPermissions = true
                ActivityCompat.requestPermissions(
                    activity, deniedPermissions.toTypedArray(), REQUEST_PERMISSIONS
                )
            }
            false
        }
    }


    private fun openAppSettings() {
        returnedFromSettings = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (returnedFromSettings) {
            returnedFromSettings = false
            hasRequestedPermissions = false
        }

        if (checkAndRequestPermissions(this)) {
            proceedToMainScreen()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun proceedToMainScreen() {
        if (isInitialized) return
        isInitialized = true
        setupViews()
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()

                binding.llEmergencyContainer.visibility = View.VISIBLE
                isScheduleCall = false
                Constant.isClickType = 1
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "<<Back to compose"

                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.GONE
                if (Constant.isClickType == 2) {
                    binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                    binding.gridViewScheduleCall.visibility = View.VISIBLE
                } else {
                    binding.rlaScheduleCallPickDate.visibility = View.GONE
                    binding.gridViewScheduleCall.visibility = View.GONE
                }
                binding.rlaMessageFromText.visibility = View.GONE
                binding.rlaSendText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE

                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage, binding.imgVoiceMessage, binding.lblVoiceMessage
                )
            }

            R.id.rlaScheduleCall -> {
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()

                binding.llEmergencyContainer.visibility = View.GONE
                isScheduleCall = true
                Constant.isClickType = 2
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "<<Back to compose"

                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                if (Constant.isClickType == 2) {
                    binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                    binding.gridViewScheduleCall.visibility = View.VISIBLE
                } else {
                    binding.rlaScheduleCallPickDate.visibility = View.GONE
                    binding.gridViewScheduleCall.visibility = View.GONE
                }
                binding.rlaMessageFromText.visibility = View.GONE
                binding.rlaSendText.visibility = View.GONE
                binding.rlaRecordVoice.visibility = View.VISIBLE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall, binding.imgScheduleCall, binding.lblScheduleCall
                )
            }

            R.id.rlaTextMessage -> {
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = 0
                binding.SwitchEmergencyVoice.setChecked(false)
                changeLabel()

                binding.llEmergencyContainer.visibility = View.GONE
                isScheduleCall = false
                Constant.isClickType = 3
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = "<<Back to compose"
                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.rlaMessageFromText.visibility = View.VISIBLE
                binding.rlaSendText.visibility = View.VISIBLE
                if (Constant.isClickType == 2) {
                    binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
                    binding.gridViewScheduleCall.visibility = View.VISIBLE
                } else {
                    binding.rlaScheduleCallPickDate.visibility = View.GONE
                    binding.gridViewScheduleCall.visibility = View.GONE
                }
                binding.rlaRecordVoice.visibility = View.GONE
                binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage, binding.imgTextMessage, binding.lblTextMessage
                )
            }

            R.id.rlaFromTime -> {
                showTimePickerDialog(this, this)
            }

            R.id.rlaAddLocalFile -> {
                stopAudioProgressUpdate()
                openAudioFilePicker()
            }

            R.id.imgClose -> {
                binding.lblDurationOfVoice.text = "00:00 / 03:00"
                binding.rlaSeekBarAndTitle.visibility = View.GONE
                binding.rlaTitle.visibility = View.GONE
                Constant.isVoiceFile = ""
                binding.rlaAddLocalFile.visibility = View.VISIBLE
//                binding.imgVoiceRecord.visibility = View.VISIBLE
                binding.rytVoiceRecord.visibility = View.VISIBLE
                binding.lblDurationOfVoice.visibility = View.VISIBLE
            }

            R.id.rlaSendText -> {
                if (binding.edtTitleTextMessage.text.toString() != "") {
                    if (binding.edtContentTextMessage.text.toString() != "") {
                        isGoToRecipient()
                    } else {
                        Constant.showValidationAlertPopup("Enter title and description", this)
                    }
                } else {
                    Constant.showValidationAlertPopup("Enter title and description", this)
                }
            }

            R.id.rlaToTime -> {
                showTimePickerDialog(this, this)
            }

            R.id.rlaAcademicYear -> {
                showAcademicDropdown(
                    binding.rlaAcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
                    isAcademicYearId = selectedYear.id
                }
            }

            R.id.rlaSendVoice -> {
                if (!Constant.isVoiceFile.equals("")) {
                    if (binding.edtTitle.text.toString() != "") {
                        isGoToRecipient()
                    } else {
                        Constant.showValidationAlertPopup("Voice and title is required.", this)
                    }
                } else {
                    Constant.showValidationAlertPopup("Voice and title is required.", this)
                }
            }

            R.id.imgVoicePlay -> {
                if (isPlayingVoice && mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    stopAudioProgressUpdate()
                    lastPosition = mediaPlayer!!.currentPosition
                    isPlayingVoice = false
                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.video_play)
                    )
                } else {
                    Log.d("AudioDebug", "Play button clicked, isPrepared=$isPrepared")
                    Log.d("AudioDebug", "audioFilePath = $audioFilePath")

                    val normalizedPower = max(1f, (1f + 160) / 160)
                    binding.waveformSeekBar.updateWithLevel(normalizedPower)

                    if (!isPrepared) {
                        initializeMediaPlayer()
                    } else {
                        mediaPlayer?.let {
                            it.seekTo(lastPosition)
                            it.start()
                            isPlayingVoice = true
                            binding.imgVoicePlay.setImageDrawable(
                                ContextCompat.getDrawable(this, R.drawable.pause_icon)
                            )
                            startAudioProgressUpdate()
                        } ?: Log.e("AudioDebug", "mediaPlayer is null on resume!")
                    }
                }
            }


            R.id.imgVoiceRecord -> {
                stopAudioProgressUpdate()
//                if (!isRecording) {
                    Constant.isVoiceType = 1
                    startRecording()
//                } else {
//                    stopRecording()
//                }
            }

            R.id.lottieAnimationView -> {
                stopAudioProgressUpdate()
                stopRecording()
            }

            R.id.infosymbol -> {
                binding.infosymbol.setOnClickListener {
                    val popupView = layoutInflater.inflate(R.layout.custom_tooltip, null)

                    val popupWindow = PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                    )

                    popupWindow.elevation = 10f

                    popupWindow.showAsDropDown(binding.infosymbol, -20, 10)
                }
            }


            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrScheduleCall -> {
                val dateAdapter = DateAdapter(this) { updatedList ->
                    // Optional callback when dates are clicked inside calendar
                }

                selectedDatesAdapter = SelectedDatesAdapter(
                    context = this,
                    selectedDates = selectedDates.toMutableList(),
                    dateAdapter = dateAdapter
                ) { removedDate ->
                    // Properly update the selectedDates list
                    selectedDates.remove(removedDate)

                    // Update the DateAdapter too
                    dateAdapter.removeSelectedDate(removedDate)
                }

                binding.gridViewScheduleCall.adapter = selectedDatesAdapter

                val datePickerPopup = CustomDatePicker(
                    context = this,
                    preSelectedDates = selectedDates.toList(), // now synced
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

                Log.d("Constant.isClickType", Constant.isClickType.toString())
                when (Constant.isClickType) {
                    1 -> {
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaScheduleCallPickDate.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.VISIBLE
                        binding.llEmergencyContainer.visibility = View.VISIBLE
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
                stopAudioProgressUpdate()
                when (Constant.isClickType) {
                    1 -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        isGetVoiceHistory()
                    }

                    2 -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.VISIBLE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        isGetVoiceHistory()
                    }

                    else -> {
                        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                        binding.lnrHistoryList.visibility = View.GONE
                        binding.rlaBackRecord.visibility = View.VISIBLE
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.GONE
                        binding.rlaMessageFromText.visibility = View.GONE
                        binding.rlaSendText.visibility = View.GONE
                        isGetTextHistory()
                    }
                }
            }
        }
    }

    private fun infosymbolload(): PopupWindow {
        val popupView = layoutInflater.inflate(R.layout.custom_tooltip, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(binding.infosymbol, -20, 10)

        return popupWindow
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
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                isSaveTheVoiceData()
                isSaveTheTextData()
                startActivity(intent)
            } else {
                val intent = Intent(this, RecipientActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                isSaveTheVoiceData()
                isSaveTheTextData()
                startActivity(intent)
            }
        } else {
            val intent = Intent(this, RecipientActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            isSaveTheVoiceData()
            isSaveTheTextData()
            startActivity(intent)
        }
    }

    fun isSaveTheVoiceData() {
        val voiceData = VoiceSendingData(
            isFilePath = Constant.isVoiceFile,
            isClickType = Constant.isClickType,
            selectedDates = selectedDates,
            isStartTimeText = binding.lblStartTime.text.toString(),
            isEndTimeText = binding.lblEndTime.text.toString(),
            title = binding.edtTitle.text.toString(),
            isEmergency = isEmergency,
            isScheduleCall = isScheduleCall,
            isAwsUrl = audioFilePath.toString(),
            isFileName = isFileName.toString()
        )
        Constant.isVoiceSendingData = voiceData
    }

    fun isSaveTheTextData() {
        val isTextData = TextSendingData(
            isTitle = binding.edtTitleTextMessage.text.toString(),
            isContent = binding.edtContentTextMessage.text.toString(),
        )
        Constant.isTextSendingData = isTextData
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

        Log.d("isVoiceHistory", "isVoiceHistory")
        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
        mAdapter = VoiceHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false
        binding.rcyHistoryDataVoiceAndText.adapter = mAdapter

        appViewModel!!.isGetVoiceHistory(isAccessToken!!, "0", this)
    }

    private fun isGetTextHistory() {

        binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
        mTextAdapter = TextHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHistoryDataVoiceAndText.layoutManager = LinearLayoutManager(this)
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false;
        binding.rcyHistoryDataVoiceAndText.adapter = mTextAdapter

        appViewModel!!.isGetTextHistory(isAccessToken!!, this)
    }


    private fun loadVoiceData(isVoiceHistoryData: List<VoiceHistoryDetails>) {
         mAdapter =
                VoiceHistoryAdapter(isVoiceHistoryData, this, this, Constant.isShimmerViewDisable)
            binding.rcyHistoryDataVoiceAndText.adapter = mAdapter
    }

    override fun onItemClick(data: TextDetail, holder: TextHistoryAdapter.DataViewHolder) {

        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
        binding.lnrHistoryList.visibility = View.VISIBLE
        binding.rlaBackRecord.visibility = View.GONE
        binding.gridViewScheduleCall.visibility = View.VISIBLE
        binding.rlaRecordVoice.visibility = View.GONE
        binding.rlaMessageFromText.visibility = View.VISIBLE
        binding.rlaSendText.visibility = View.VISIBLE
        binding.edtTitleTextMessage.setText(data.title.toString())
        binding.edtContentTextMessage.setText(data.content.toString())
    }

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblStartTime.text = String.format("%02d:%02d %s", hour, minute, amPm)
        binding.lblEndTime.text = String.format("%02d:%02d %s", hour, minute, amPm)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(
        data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder
    ) {
        // UI setup
        if (Constant.isClickType == 2) {
            binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
            binding.gridViewScheduleCall.visibility = View.VISIBLE
        } else {
            binding.rlaScheduleCallPickDate.visibility = View.GONE
            binding.gridViewScheduleCall.visibility = View.GONE
        }
        binding.rlaRecordVoice.visibility = View.VISIBLE

        binding.llEmergencyContainer.visibility = View.VISIBLE
        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
        binding.rlaBackRecord.visibility = View.GONE
        binding.lnrHistoryList.visibility = View.VISIBLE

//        binding.imgVoiceRecord.visibility = View.GONE
        binding.rytVoiceRecord.visibility = View.GONE
        binding.lblDurationOfVoice.visibility = View.GONE
        binding.rlaAddLocalFile.visibility = View.GONE


        binding.imgVoiceRecord.setImageDrawable(
            ContextCompat.getDrawable(this@CommunicationSchool, R.drawable.record_icon)
        )
        mediaRecorder = null
        isRecording = false
        recordingHandler.removeCallbacks(recordingRunnable) // Stop updating time
        Log.d(
            "RecordingFilePath", "Recording stopped. File Path: $audioFilePath"
        )
        Constant.isVoiceFile = audioFilePath
        binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
        binding.rlaTitle.visibility = View.VISIBLE
        binding.edtTitle.setText(data.title.toString())

//        binding.lblEndDuration.text = "/ "+data.duration.toString()
        binding.lblEndDuration.text = "/ " + Constant.getAudioDurationInMinutes(data.url)

        Constant.isVoiceType = 3
        // Get the URL or file path from the clicked item
        val voiceUrlOrPath =
            data.url  // Make sure this property exists in your VoiceHistoryDetails model

        // Set the audio file path globally (assuming this is used in initializeMediaPlayer)
        audioFilePath = voiceUrlOrPath


        val currentDate: String? = Constant.getCurrentDate()
        val isFileExtension = getFileExtensionFromAwsUrl(data.url)
        isFileName = "sss_" + currentDate + "." + isFileExtension
    }


    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_AUDIO_REQUEST)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val mediaPlayer = MediaPlayer()
                try {
                    // Set data source to get duration
                    mediaPlayer.setDataSource(this, uri)
                    mediaPlayer.prepare()
                    val durationInMillis = mediaPlayer.duration
                    val formattedDuration = formatDuration(durationInMillis)
                    mediaPlayer.release()
                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                 var   isFileExtension = "mp3"
                    val fileName = "Communication_${timeStamp}.$isFileExtension"
                    isFileName = fileName
                    // Copy file to app cache
                    val inputStream = contentResolver.openInputStream(uri)
                    val outputFile = File(cacheDir, fileName)
                    val outputStream = FileOutputStream(outputFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    // Store local path for upload/use
                    audioFilePath = outputFile.absolutePath
                    Constant.isVoiceType = 2
                    Constant.isVoiceFile = audioFilePath

                    // Update UI
                    binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
                    binding.rlaTitle.visibility = View.VISIBLE
//                    binding.imgVoiceRecord.visibility = View.GONE
                    binding.rytVoiceRecord.visibility = View.GONE
                    binding.lblDurationOfVoice.visibility = View.GONE
                    binding.rlaAddLocalFile.visibility = View.GONE
                    binding.lblEndDuration.text = "/ $formattedDuration"

                } catch (e: Exception) {
                    mediaPlayer.release()
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getFileExtensionFromAwsUrl(url: String): String? {
        val fileName = url.substringAfterLast("/")
        return getFileExtension(fileName)
    }

    fun getFileExtension(fileName: String): String? {
        return if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf(".") + 1)
        } else {
            null
        }
    }
}