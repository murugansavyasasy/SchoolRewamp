package com.vs.schoolmessenger.School.Communication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.KeyguardManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Communication.Adapter.DateAdapter
import com.vs.schoolmessenger.School.Communication.Adapter.SelectedDatesAdapter
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter
import com.vs.schoolmessenger.School.Communication.Adapter.VoiceHistoryAdapter
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetail
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendingData
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceHistoryDetails
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceSendingData
import com.vs.schoolmessenger.School.Communication.Interface.TextHistoryClickListener
import com.vs.schoolmessenger.School.Communication.Interface.VoiceHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.CustomDatePicker
import com.vs.schoolmessenger.Utils.FileExtensionFromContentUri
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.KeyboardUtils
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener,
    VoiceHistoryClickListener, TextHistoryClickListener, TimeSelectedListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    private val audioHandler = Handler(Looper.getMainLooper())
    private var audioProgressRunnable: Runnable? = null


    private var fromHour24: Int? = null
    private var fromMinute: Int? = null
    private var toHour24: Int? = null
    private var toMinute: Int? = null
    private var isFromTime = true


    private var isInitialized = false
    private var selectedDatesAdapter: SelectedDatesAdapter? = null

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null
    private var isPlayingVoice = false
    private var lastPosition: Int = 0
    var mediaPlayer: MediaPlayer? = null
    private val REQUEST_PERMISSIONS = 100
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
    var isEmergency = false
    var isScheduleCall = false
    private val PICK_AUDIO_REQUEST = 101
    var isAcademicYearId = -1
    var isAcademicYear: List<AcademicYear>? = null
    var isFileName: String? = null

    //    var isFromTime = true
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
    private var recordingStartTime: Long = 0


    @SuppressLint("ClickableViewAccessibility", "DefaultLocale")
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        Constant.isCommunicationType = 1
        Constant.isVoiceType == 1

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
        binding.toolbarLayout.imgBack.setOnClickListener(this)
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
        recordingStartTime = System.currentTimeMillis()
        isUserDetails = SharedPreference.getUserDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE

        if (isUserDetails!!.staff_role == Constant.isStaffRole) {
            binding.rlaScheduleCall.visibility = View.GONE
        } else {
            binding.rlaScheduleCall.visibility = View.VISIBLE
        }
        if(isUserDetails!!.staff_role == Constant.isStaffRole || isUserDetails!!.staff_role == Constant.isNonTeachingStaffRole){
            binding.llEmergencyContainer.visibility = View.GONE
        }
        else{
            binding.llEmergencyContainer.visibility = View.VISIBLE
        }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName

        if (!checkAndRequestPermissions(this)) {
            return
        }
        mediaRecorder = MediaRecorder()

        isFileExtensionFromContentUri = FileExtensionFromContentUri()

        isMultipleSchool = isUserDetails!!.staff_details.size > 1

//        binding.lblStartTime.text = Constant.getCurrentTime()
//        binding.lblEndTime.text = Constant.getTimeAfter20Minutes()

        appViewModel!!.isGetVoiceHistory?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    binding.rytNORecordFound.visibility = View.GONE
                    binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                    binding.rlaRecordVoice.visibility = View.GONE
                    binding.rlaMessageFromText.visibility = View.GONE
                    val isGetHistory = response.data
                    isVoiceHistoryData = isGetHistory
                    loadVoiceData(isVoiceHistoryData)
                } else {
                    binding.rytNORecordFound.visibility = View.VISIBLE
                    binding.lblNoRecordFound.text = response.message
                    binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                }
            }
        }

        appViewModel!!.isGetTextHistory?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    binding.rytNORecordFound.visibility = View.GONE
                    binding.rcyHistoryDataVoiceAndText.visibility = View.VISIBLE
                    binding.rlaRecordVoice.visibility = View.GONE
                    binding.rlaMessageFromText.visibility = View.GONE
                    val isTextHistory = response.data
                    isTextHistoryData = isTextHistory
                    loadTextHistoryData(isTextHistoryData)
                } else {
                    binding.rytNORecordFound.visibility = View.VISIBLE
                    binding.lblNoRecordFound.text = response.message
                    binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
                }
            }
        }

        changeLabel()
        binding.SwitchEmergencyVoice.setOnClickListener {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
            }
            if (isRecording) {
                stopRecording()
            }
            binding.rlaSeekBarAndTitle.visibility = View.GONE
//            binding.rlaTitle.visibility = View.GONE
            Constant.selectedFiles.clear()
            binding.rlaAddLocalFile.visibility = View.VISIBLE
            binding.rytVoiceRecord.visibility = View.VISIBLE
            binding.lblDurationOfVoice.visibility = View.VISIBLE
            if (binding.SwitchEmergencyVoice.isChecked()) {
                Constant.isAccessType = Constant.isEmergency
                binding.lblDurationOfVoice.text = Constant._00_00_00_30
                isEmergency = true
                MAX_RECORDING_TIME = 30
                val popupWindow = infosymbolload()

                Handler(Looper.getMainLooper()).postDelayed({
                    popupWindow.dismiss() // Dismiss the tooltip after 2 seconds
                }, 2000)
            } else {
                Constant.isAccessType = Constant.isNonEmergency
                binding.lblDurationOfVoice.text = Constant._00_00_03_00
                isEmergency = false
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
                    Constant._02d__02d_s,
                    recordingTime / 60,
                    recordingTime % 60,
                    if (MAX_RECORDING_TIME == 30) Constant._00_30 else Constant._03_00
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

        Constant.setupEditTextWithScroll(
            this, binding.scrollRoot, binding.edtContentTextMessage
        )
        Constant.setupEditTextWithScroll(
            this, binding.scrollRoot, binding.edtTitle
        )

        binding.waveformSeekBar.setOnSeekChangeListener { progress ->

            val player = mediaPlayer ?: return@setOnSeekChangeListener
            if (!isPrepared || player.duration <= 0) return@setOnSeekChangeListener

            val newPosition = (progress * player.duration).toInt()
            player.seekTo(newPosition)
            lastPosition = newPosition
            updateCurrentTime(newPosition)

            if (!player.isPlaying) {
                player.start()
                startAudioProgressUpdate()
                binding.imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pause_icon)
                )
            }
        }

        initializeDefaultTimes()
    }

    private fun initializeDefaultTimes() {
        val fromCal = Calendar.getInstance()
        fromCal.add(Calendar.MINUTE, 10)
        fromHour24 = fromCal.get(Calendar.HOUR_OF_DAY)
        fromMinute = fromCal.get(Calendar.MINUTE)
        val toCal = fromCal.clone() as Calendar
        toCal.add(Calendar.MINUTE, 40)
        toHour24 = toCal.get(Calendar.HOUR_OF_DAY)
        toMinute = toCal.get(Calendar.MINUTE)
        binding.lblStartTime.text = formatTime12h(fromHour24!!, fromMinute!!)
        binding.lblEndTime.text = formatTime12h(toHour24!!, toMinute!!)
    }

    private fun formatTime12h(hour24: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    private fun showTimePickerDialog(
        context: Context,
        listener: TimeSelectedListener,
        preSelectedHour: Int?,
        preSelectedMinute: Int?
    ) {
        val calendar = Calendar.getInstance()

        val hour = preSelectedHour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = preSelectedMinute ?: calendar.get(Calendar.MINUTE)

        var isTimeSelected = false

        val timePicker = TimePickerDialog(
            context, { _, selectedHour, selectedMinute ->

                isTimeSelected = true

                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                }

                val today = SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(Date())

                val isTodaySelected = selectedDates.contains(today)

                if (isFromTime) {

                    // 🔒 Only NOW-based restriction depends on today
                    if (isTodaySelected) {
                        val minAllowedCal = Calendar.getInstance()
                        minAllowedCal.add(Calendar.MINUTE, 10)

                        if (selectedCal.before(minAllowedCal)) {
                            Toast.makeText(
                                this,
                                "From time must be at least 10 minutes from now",
                                Toast.LENGTH_LONG
                            ).show()
                            return@TimePickerDialog
                        }
                    }

                    fromHour24 = selectedHour
                    fromMinute = selectedMinute

                    // Auto TO = FROM + 40
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        add(Calendar.MINUTE, 40)
                    }

                    toHour24 = cal.get(Calendar.HOUR_OF_DAY)
                    toMinute = cal.get(Calendar.MINUTE)

                    binding.lblStartTime.text =
                        formatTime12h(fromHour24!!, fromMinute!!)

                    binding.lblEndTime.text =
                        formatTime12h(toHour24!!, toMinute!!)
                }
                else {

                    if (fromHour24 != null && fromMinute != null) {

                        val minToCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, fromHour24!!)
                            set(Calendar.MINUTE, fromMinute!!)
                            set(Calendar.SECOND, 0)
                            add(Calendar.MINUTE, 40)
                        }

                        // 🔥 ALWAYS enforce 40-minute gap
                        if (selectedCal.before(minToCal)) {
                            Toast.makeText(
                                this,
                                "End time must be at least 40 minutes after start time",
                                Toast.LENGTH_LONG
                            ).show()
                            return@TimePickerDialog
                        }
                    }

                    toHour24 = selectedHour
                    toMinute = selectedMinute

                    binding.lblEndTime.text =
                        formatTime12h(toHour24!!, toMinute!!)
                }

            }, hour, minute, false
        )

        // 🔒 Handle close without selection
        timePicker.setOnCancelListener {
            if (!isFromTime && !isTimeSelected &&
                fromHour24 != null && fromMinute != null
            ) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, fromHour24!!)
                    set(Calendar.MINUTE, fromMinute!!)
                    add(Calendar.MINUTE, 40)
                }

                toHour24 = cal.get(Calendar.HOUR_OF_DAY)
                toMinute = cal.get(Calendar.MINUTE)

                binding.lblEndTime.text =
                    formatTime12h(toHour24!!, toMinute!!)
            }
        }

        timePicker.show()
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
        AlertDialog.Builder(this).setTitle(getString(R.string.PermissionsRequired))
            .setMessage(getString(R.string.permissions_permanently_denied_proceed))
            .setPositiveButton(getString(R.string.permission_ok)) { _, _ ->
                openAppSettings()
            }.setCancelable(false).show()
    }

    private fun changeLabel() {
        binding.lblSend.text = resources.getString(R.string.NEXT)
        if (Constant.isEmergencyVoiceNoticeBoard == true) {
            if (isMultipleSchool) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                if (!isEmergency) {
                    binding.lblSend.text = resources.getString(R.string.NEXT)
                } else {
                    binding.lblSend.text = resources.getString(R.string.Send)
                }
            }
        } else {
            if (!isEmergency) {
                binding.lblSend.text = resources.getString(R.string.NEXT)
            } else {
                binding.lblSend.text = resources.getString(R.string.Send)
            }
        }
    }

    private fun startRecording() {
        keepScreenOn()
        Constant.selectedFiles.clear()
        if (checkAndRequestPermissions(this)) {
            val dir = externalCacheDir ?: cacheDir
            val timeStamp =
                SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())
            val fileName = "${Constant.original_}$timeStamp${Constant.m4a}"
            val filePath = "${dir.absolutePath}/$fileName"
            audioFilePath = filePath
            Log.d("recordedFilePath", filePath)
            isFileName = fileName
            Constant.isVoiceType = 1
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                    recordingTime = 0
                    recordingStartTime = System.currentTimeMillis()
                    recordingRunnable = object : Runnable {
                        @SuppressLint("DefaultLocale")
                        override fun run() {
                            if (isRecording) {
                                recordingTime++
                                binding.lblDurationOfVoice.text = String.format(
                                    Constant._02d__02d_s,
                                    recordingTime / 60,
                                    recordingTime % 60,
                                    if (MAX_RECORDING_TIME == 30) Constant._00_30 else Constant._03_00
                                )
                                if (recordingTime >= MAX_RECORDING_TIME) {
                                    stopRecording()
                                } else {
                                    recordingHandler.postDelayed(this, 1000)
                                }
                            }
                        }
                    }
                    recordingHandler.post(recordingRunnable)
                    binding.lottieAnimationView.visibility = View.VISIBLE
                    binding.imgVoiceRecord.visibility = View.GONE
                    binding.lottieAnimationView.setAnimation(R.raw.voice_record)
                    binding.lottieAnimationView.loop(true)
                    binding.lottieAnimationView.playAnimation()
                    binding.lblDurationOfVoice.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            openAppSettings()
        }
    }

    private fun stopRecording() {
        val elapsedTime = System.currentTimeMillis() - recordingStartTime
        if (elapsedTime < 1000L) {
            Handler(Looper.getMainLooper()).postDelayed({
                stopRecording()
            }, 1000L - elapsedTime)
            return
        }
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
                if (file.exists() && file.length() > 0L) {
                    Constant.selectedFiles?.add(FileItem(audioFilePath.toString(), FileType.AUDIO))
                    Constant.isVoiceType = 1
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(audioFilePath)
                    mediaPlayer.prepare()
                    val durationInMs = mediaPlayer.duration
                    mediaPlayer.release()
                    val formattedDuration = formatDuration(durationInMs)
                    binding.lblEndDuration.text = "/ $formattedDuration"
                    binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
//                    binding.edtTitle.setText("")
                    binding.rlaTitle.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        this@CommunicationSchool,
                        getString(R.string.Recording_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@CommunicationSchool,
                    getString(R.string.Failed_recording),
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
                if (audioFilePath!!.startsWith(Constant.content) || audioFilePath!!.startsWith(
                        Constant.file
                    )
                ) {
                    setDataSource(this@CommunicationSchool, uri)
                } else if (audioFilePath!!.startsWith(Constant.http)) {
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
                    binding.lblEndDuration.text = "/ " + totalFormatted
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
                        Log.d("currentFormatted", currentFormatted.toString())
                        binding.lblStartDuration.text = currentFormatted
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }, 1000)
    }

    private fun formatDuration(durationInMillis: Int): String {
        val adjustedDuration = ceil(durationInMillis / 1000.0).toInt() // more accurate
        val minutes = adjustedDuration / 60
        val seconds = adjustedDuration % 60
        return String.format(Constant.dateForMate, minutes, seconds)
    }

    private fun startAudioProgressUpdate() {
        audioProgressRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {

                        val progress = player.currentPosition.toFloat() / player.duration.toFloat()

                        binding.waveformSeekBar.updateWithLevel(progress)
                        updateCurrentTime(player.currentPosition)

                        audioHandler.postDelayed(this, 50) // smooth like WhatsApp
                    }
                }
            }
        }
        audioHandler.post(audioProgressRunnable!!)
    }


    private fun stopAudioProgressUpdate() {
        handler.removeCallbacks(progressUpdater)
        //   binding.waveformSeekBar.updateWithLevel(0f)
        binding.imgVoicePlay.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.video_play)
        )
        audioProgressRunnable?.let {
            audioHandler.removeCallbacks(it)
        }
    }

    fun checkAndRequestAccessFilePermissions(activity: Activity): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

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

    fun checkAndRequestPermissions(activity: Activity): Boolean {
        val permissions = mutableListOf<String>()
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
        intent.data = Uri.fromParts(Constant.packagename, packageName, null)
        startActivity(intent)
    }

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

    private fun proceedToMainScreen() {
        if (isInitialized) return
        isInitialized = true
        setupViews()
    }

    fun isClearData() {
        if (isRecording) {
            stopRecording()
        }
        binding.SwitchEmergencyVoice.setChecked(false)
        isEmergency = false
        MAX_RECORDING_TIME = 180
        binding.lblDurationOfVoice.text = Constant._00_00_03_00
        binding.rlaSeekBarAndTitle.visibility = View.GONE
//        binding.rlaTitle.visibility = View.GONE
        binding.lblStartDuration.text = Constant.time_zero
        binding.lblEndDuration.text = ""
        binding.waveformSeekBar.updateWithLevel(0f)
//        binding.edtTitle.setText("")
        binding.rlaAddLocalFile.visibility = View.VISIBLE
        binding.rytVoiceRecord.visibility = View.VISIBLE
        binding.lblDurationOfVoice.visibility = View.VISIBLE
        binding.imgVoiceRecord.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.record_icon)
        )

        isRecording = false
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        mediaPlayer = null
        isPrepared = false
        isPlayingVoice = false
        lastPosition = 0
        recordingTime = 0


        audioFilePath = null
        isFileName = null
        Constant.isVoiceType = 0
        Constant.selectedFiles.clear()


        handler.removeCallbacks(progressUpdater)
        recordingHandler.removeCallbacks(recordingRunnable)
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
                selectedDates.clear()
                selectedDatesAdapter?.submitSelectedDates(emptyList())
                KeyboardUtils.hideKeyboard(this)
                Constant.isEmergencyVoiceNoticeBoard = false
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = false
                if (binding.SwitchEmergencyVoice.isChecked()) {
                    binding.SwitchEmergencyVoice.setChecked(true)
                } else {
                    binding.SwitchEmergencyVoice.setChecked(false)
                }
                changeLabel()
                if(isUserDetails!!.staff_role == Constant.isStaffRole || isUserDetails!!.staff_role == Constant.isNonTeachingStaffRole){
                    binding.llEmergencyContainer.visibility = View.GONE
                }
                else{
                    binding.llEmergencyContainer.visibility = View.VISIBLE
                }
//                binding.llEmergencyContainer.visibility = View.VISIBLE
                isScheduleCall = false
                Constant.isCommunicationType = 1
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = getString(R.string.back_to_compose)
                binding.rlaBackRecord.visibility = View.GONE
                binding.gridViewScheduleCall.visibility = View.GONE
                if (Constant.isCommunicationType == 2) {
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
                selectedDates.clear()
                selectedDatesAdapter?.submitSelectedDates(emptyList())
                KeyboardUtils.hideKeyboard(this)
                binding.lblDurationOfVoice.text = Constant._00_00_03_00
                if (binding.SwitchEmergencyVoice.isChecked() == true) {
                    binding.SwitchEmergencyVoice.setChecked(true)
                } else {
                    binding.SwitchEmergencyVoice.setChecked(false)
                }
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = false
                changeLabel()


                binding.llEmergencyContainer.visibility = View.GONE
                isScheduleCall = true
                Constant.isCommunicationType = 2
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = getString(R.string.back_to_compose)
                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                if (Constant.isCommunicationType == 2) {
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
                KeyboardUtils.hideKeyboard(this)
                Constant.isAccessType = Constant.isNonEmergency
                isEmergency = false
                if (binding.SwitchEmergencyVoice.isChecked()) {
                    binding.SwitchEmergencyVoice.setChecked(true)
                } else {
                    binding.SwitchEmergencyVoice.setChecked(false)
                }
                changeLabel()
                binding.llEmergencyContainer.visibility = View.GONE
                isScheduleCall = false
                Constant.isCommunicationType = 3
                selectedDates.clear()
                selectedDatesAdapter?.submitSelectedDates(emptyList())
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                binding.lblBackToVoiceMessage.text = getString(R.string.back_to_compose)
                binding.rlaBackRecord.visibility = View.GONE
                binding.lnrHistoryList.visibility = View.VISIBLE
                binding.rlaMessageFromText.visibility = View.VISIBLE
                binding.rlaSendText.visibility = View.VISIBLE
                if (Constant.isCommunicationType == 2) {
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

            // Click handlers
            R.id.rlaFromTime -> {
                KeyboardUtils.hideKeyboard(this)
                isFromTime = true
                showTimePickerDialog(
                    this, this, // assuming activity implements TimeSelectedListener
                    fromHour24, fromMinute
                )
            }

            R.id.rlaToTime -> {
                KeyboardUtils.hideKeyboard(this)
                isFromTime = false

                val preHour = toHour24 ?: fromHour24
                val preMin = toMinute ?: fromMinute?.plus(40)?.let {
                    if (it >= 60) it - 60 else it
                }
                val carryHour = if (fromMinute != null && fromMinute!! + 40 >= 60) 1 else 0

                showTimePickerDialog(
                    this,
                    this,
                    preHour?.plus(carryHour),
                    preMin
                )
            }

            R.id.lnrScheduleCall -> {
                KeyboardUtils.hideKeyboard(this)
                val dateAdapter = DateAdapter(this) { updatedList -> }
                selectedDatesAdapter = SelectedDatesAdapter(
                    context = this,
                    selectedDates = selectedDates.toMutableList(),
                    dateAdapter = dateAdapter
                ) { removedDate ->
                    selectedDates.remove(removedDate)
                    dateAdapter.removeSelectedDate(removedDate)
                }
                binding.gridViewScheduleCall.adapter = selectedDatesAdapter
                val datePickerPopup = CustomDatePicker(
                    context = this,
                    preSelectedDates = selectedDates.toList(),
                    dateAdapter = dateAdapter
                ) { newSelectedDates ->
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val tf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val pickedTimeText = binding.lblStartTime.text.toString()
                    val now = Calendar.getInstance()
                    val validDates = mutableListOf<String>()
                    newSelectedDates.forEach { dateStr ->
                        val selectedCal = Calendar.getInstance()
                        selectedCal.time = sdf.parse(dateStr)!!
                        val isToday =
                            now.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) && now.get(
                                Calendar.DAY_OF_YEAR
                            ) == selectedCal.get(Calendar.DAY_OF_YEAR)
                        if (isToday && pickedTimeText.isNotEmpty()) {
                            val pickedTimeOnly = tf.parse(pickedTimeText)!!
                            val pickedCal = Calendar.getInstance().apply {
                                time = pickedTimeOnly
                                set(Calendar.YEAR, selectedCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, selectedCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
                            }
                            if (pickedCal.before(now)) {
                                Toast.makeText(
                                    this,
                                    "You cannot select today's date with past time.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                validDates.add(dateStr)
                            }
                        } else {
                            validDates.add(dateStr)
                        }
                    }
                    selectedDates.clear()
                    selectedDates.addAll(validDates)
                    selectedDatesAdapter?.submitSelectedDates(validDates)
                }

                datePickerPopup.show(window.decorView.rootView)
            }


            R.id.rlaAddLocalFile -> {

                if (checkAndRequestAccessFilePermissions(this)) {
                    KeyboardUtils.hideKeyboard(this)
                    stopAudioProgressUpdate()
                    mediaPlayer?.let {
                        if (it.isPlaying) it.stop()
                        it.reset()
                    }
                    lastPosition = 0
                    isPlayingVoice = false
                    binding.lblStartDuration.text = "00:00"
                    Constant.selectedFiles.clear()
                    openAudioFilePicker()
                }
            }

            R.id.imgClose -> {
                KeyboardUtils.hideKeyboard(this)
                isClearData()
            }

            R.id.rlaSendText -> {
                KeyboardUtils.hideKeyboard(this)
                val title = binding.edtTitleTextMessage.text.toString().trim()
                val description = binding.edtContentTextMessage.text.toString().trim()
                if (title.isEmpty()) {
                    binding.edtTitleTextMessage.error = getString(R.string.This_field_required)
                    binding.edtTitleTextMessage.requestFocus()
                    return
                }
                if (description.isEmpty()) {
                    binding.edtContentTextMessage.error = getString(R.string.This_field_required)
                    binding.edtContentTextMessage.requestFocus()
                    return
                }
                isGoToRecipient()
            }

            R.id.rlaAcademicYear -> {
                KeyboardUtils.hideKeyboard(this)
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
                KeyboardUtils.hideKeyboard(this)

                if (Constant.isVoiceType == 3) {
                    if (Constant.selectedFiles.isNotEmpty()) {
                        if (binding.edtTitle.text.toString().isNotBlank()) {
                            if (isScheduleCall) {
                                if (binding.lblStartTime.text.toString() != "Select time" && binding.lblEndTime.text.toString() != "Select time") {

                                    if (isPastTimeForToday()) {
                                        Constant.showValidationAlertPopup(
                                            getString(R.string.alert),
                                            getString(R.string.do_not_allow_past_time),
                                            this
                                        )
                                        return
                                    }

                                    if (selectedDates.isNotEmpty()) {
                                        isGoToRecipient()
                                    } else {
                                        Constant.showValidationAlertPopup(
                                            getString(R.string.alert),
                                            getString(R.string.Select_schedule_date),
                                            this
                                        )
                                    }
                                } else {
                                    Constant.showValidationAlertPopup(
                                        getString(R.string.alert),
                                        getString(R.string.select_the_time),
                                        this
                                    )
                                }
                            } else {
                                isGoToRecipient()
                            }
                        } else {
                            binding.edtTitle.error = getString(R.string.This_field_required)
                        }
                    } else {
                        Constant.showValidationAlertPopup(
                            getString(R.string.alert),
                            getString(R.string.Voice_title_required),
                            this
                        )
                    }
                } else {
                    if (Constant.selectedFiles.isNotEmpty()) {
                        if (binding.edtTitle.text.toString().isNotBlank()) {
                            if (isScheduleCall) {
                                if (binding.lblStartTime.text.toString() != "Select time" && binding.lblEndTime.text.toString() != "Select time") {

                                    if (isPastTimeForToday()) {
                                        Constant.showValidationAlertPopup(
                                            getString(R.string.alert),
                                            getString(R.string.do_not_allow_past_time),
                                            this
                                        )
                                        return
                                    }

                                    if (selectedDates.isNotEmpty()) {
                                        isGoToRecipient()
                                    } else {
                                        Constant.showValidationAlertPopup(
                                            getString(R.string.alert),
                                            getString(R.string.Select_schedule_date),
                                            this
                                        )
                                    }
                                } else {
                                    Constant.showValidationAlertPopup(
                                        getString(R.string.alert),
                                        getString(R.string.select_the_time),
                                        this
                                    )
                                }
                            } else {
                                isGoToRecipient()
                            }
                        } else {
                            binding.edtTitle.error = getString(R.string.This_field_required)
                        }
                    } else {
                        Constant.showValidationAlertPopup(
                            getString(R.string.alert),
                            getString(R.string.Voice_title_required),
                            this
                        )
                    }
                }
            }

            R.id.imgVoicePlay -> {

                keepScreenOn()
                KeyboardUtils.hideKeyboard(this)

                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {

                    mediaPlayer?.pause()
                    stopAudioProgressUpdate()
                    lastPosition = mediaPlayer!!.currentPosition

                    binding.imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.video_play)
                    )

                } else {

                    if (!isPrepared) {
                        initializeMediaPlayer()
                        return
                    }

                    mediaPlayer?.let {
                        it.seekTo(lastPosition)
                        it.start()
                        startAudioProgressUpdate()

                        binding.imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.pause_icon)
                        )
                    }
                }
            }

            R.id.imgVoiceRecord -> {
                KeyboardUtils.hideKeyboard(this)
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                        player.reset()
                    }
                }
                binding.rlaAddLocalFile.visibility = View.GONE
                binding.lblStartDuration.text = Constant.time_zero
                stopAudioProgressUpdate()
                Constant.isVoiceType = 1
                startRecording()
            }

            R.id.lottieAnimationView -> {
                KeyboardUtils.hideKeyboard(this)
                stopAudioProgressUpdate()
                stopRecording()
            }

            R.id.infosymbol -> {
                KeyboardUtils.hideKeyboard(this)
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
                KeyboardUtils.hideKeyboard(this)
                onBackPressed()
                Constant.selectedFiles.clear()
            }


            R.id.rlaBackRecord -> {
                KeyboardUtils.hideKeyboard(this)
                binding.rytNORecordFound.visibility = View.GONE
                if (mAdapter != null) {
                    mAdapter!!.releaseMediaPlayer()
                }
                Log.d("Constant.isCommunicationType", Constant.isCommunicationType.toString())
                when (Constant.isCommunicationType) {
                    1 -> {
                        binding.gridViewScheduleCall.visibility = View.GONE
                        binding.rlaScheduleCallPickDate.visibility = View.GONE
                        binding.rlaRecordVoice.visibility = View.VISIBLE
                        if(isUserDetails!!.staff_role == Constant.isStaffRole || isUserDetails!!.staff_role == Constant.isNonTeachingStaffRole){
                            binding.llEmergencyContainer.visibility = View.GONE
                        }
                        else{
                            binding.llEmergencyContainer.visibility = View.VISIBLE
                        }
//                        binding.llEmergencyContainer.visibility = View.VISIBLE
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

                Constant.showLoading(this)

                Handler(Looper.getMainLooper()).postDelayed({
                    KeyboardUtils.hideKeyboard(this)
                    stopAudioProgressUpdate()
                    when (Constant.isCommunicationType) {
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
                }, 500)
            }
        }
    }

    private fun isPastTimeForToday(): Boolean {
        if (selectedDates.isEmpty()) return false
        val today = SimpleDateFormat(
            "dd-MM-yyyy", Locale.getDefault()
        ).format(Date())

        if (!selectedDates.contains(today)) return false
        val selectedCal = Calendar.getInstance()
        selectedCal.set(Calendar.HOUR_OF_DAY, fromHour24 ?: return false)
        selectedCal.set(Calendar.MINUTE, fromMinute ?: return false)
        selectedCal.set(Calendar.SECOND, 0)
        val now = Calendar.getInstance()
        return selectedCal.before(now)
    }


    private fun validateAndSetTime(hour: Int, minute: Int, amPm: String): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val currentDateStr = sdf.format(Date())

        val isTodaySelected = selectedDates.any { it == currentDateStr }

        if (isTodaySelected) {
            // Convert to 24-hour format for comparison
            val calNow = Calendar.getInstance()
            val selectedCal = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                var hour24 = hour
                if (amPm.equals("PM", ignoreCase = true) && hour < 12) hour24 += 12
                if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour24 = 0

                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, minute)
            }

            if (selectedCal.before(calNow)) {
                Toast.makeText(
                    this,
                    getString(R.string.you_cannot_select_a_past_time_for_today),
                    Toast.LENGTH_SHORT
                ).show()

                // Clear the respective label
                if (isFromTime) {
                    binding.lblStartTime.text = "Select time"
                } else {
                    binding.lblEndTime.text = "Select time"
                }
                return false
            }
        }

        return true
    }


    private fun isGoToRecipient() {
        val isStaffRole = isUserDetails!!.staff_role
        if (isMultipleSchool) {
            if (isStaffRole == Constant.isGroupHeadRole || isStaffRole == Constant.isPrincipalRole || isStaffRole == Constant.isAdminRole) {
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
            isCommunicationType = Constant.isCommunicationType,
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
        Log.d("VoiceData", Constant.isVoiceSendingData.toString())

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
        binding.edtTitle.setText("")
        KeyboardUtils.hideKeyboard(this)
        if (isRecording) {
            stopRecording()
        }
        isClearData()

        binding.lnrHistoryList.visibility = View.VISIBLE
        binding.rytNORecordFound.visibility = View.GONE
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
        binding.lblStartDuration.text = "00:00"
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
            it.reset()
        }

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
        binding.rcyHistoryDataVoiceAndText.isNestedScrollingEnabled = false
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
        // Validate before setting the text
        if (!validateAndSetTime(hour, minute, amPm)) {
            // Invalid time (past for today), so just return
            return
        }

        // If valid, update the respective label
        if (isFromTime) {
            binding.lblStartTime.text =
                String.format(Constant.timeForMateWithAMPM, hour, minute, amPm)
        } else {
            binding.lblEndTime.text =
                String.format(Constant.timeForMateWithAMPM, hour, minute, amPm)
        }
    }


    override fun onItemClick(
        data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder
    ) {
        removeSelectedVoice()
        //Latesly edited Code 13-01-2026
        //onBackPressed()  try to check in the backpressed because i have cleared  Constant.isAwsUploadedFiles.clear()
//        Constant.isAwsUploadedFiles.clear()
//
//        Constant.isAwsUploadedFiles.add(
//            AwsUploadedFiles(
//                isFileUrl = data.url, isFileType = Constant.AUDIO
//            )
//        )

        //Latesly edited Code 13-01-2026
        Log.d("isLog", data.url)
        // UI setup
        if (Constant.isCommunicationType == 2) {
            binding.rlaScheduleCallPickDate.visibility = View.VISIBLE
            binding.gridViewScheduleCall.visibility = View.VISIBLE
        } else {
            binding.rlaScheduleCallPickDate.visibility = View.GONE
            binding.gridViewScheduleCall.visibility = View.GONE
        }
        val voiceUrlOrPath = data.url
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(voiceUrlOrPath)
            mediaPlayer.prepare()
            val durationInMillis = mediaPlayer.duration
            if (Constant.isCommunicationType != 2) {
                if (binding.SwitchEmergencyVoice.isChecked()) {
                    if (durationInMillis > 30000) {
                        mediaPlayer.release()
                        showDurationLimitDialog(getString(R.string.Audio_least_30_seconds))
                        return
                    } else {
                        setHistoryData(data)
                    }
                } else {
                    if (durationInMillis > 180000) {
                        mediaPlayer.release()
                        showDurationLimitDialog(getString(R.string.Audio_below_3_minutes))
                        return
                    } else {
                        setHistoryData(data)
                    }
                }
            } else {
                if (durationInMillis > 180000) {
                    mediaPlayer.release()
                    showDurationLimitDialog(getString(R.string.Audio_below_3_minutes))
                    return
                } else {
                    setHistoryData(data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showDurationLimitDialog(getString(R.string.failed_to_load_audio_duration))
            return
        } finally {
            mediaPlayer.release()
        }
    }

    fun removeSelectedVoice() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.reset()
            it.release()
        }
        mediaPlayer = null

        if (isRecording) stopRecording()
        isRecording = false
        mediaRecorder?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            it.reset()
            it.release()
        }
        mediaRecorder = null

        binding.lottieAnimationView.cancelAnimation()
        binding.lottieAnimationView.progress = 0f
        binding.imgVoiceRecord.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.record_icon)
        )
        binding.rytVoiceRecord.visibility = View.VISIBLE
        binding.rlaAddLocalFile.visibility = View.VISIBLE
        binding.lblDurationOfVoice.visibility = View.VISIBLE
        binding.rlaSeekBarAndTitle.visibility = View.GONE
//        binding.rlaTitle.visibility = View.GONE
        binding.edtTitle.setText("")
        binding.lblStartDuration.text = Constant.time_zero
        binding.lblEndDuration.text = ""

        audioFilePath = null
        isFileName = null
        Constant.isVoiceType = 0
        Constant.selectedFiles.clear()
        recordingTime = 0
        lastPosition = 0
    }


    fun setHistoryData(data: VoiceHistoryDetails) {
        Constant.selectedFiles.clear()
        binding.rlaRecordVoice.visibility = View.VISIBLE
        if (Constant.isCommunicationType == 1) {
            if(isUserDetails!!.staff_role == Constant.isStaffRole || isUserDetails!!.staff_role == Constant.isNonTeachingStaffRole){
                binding.llEmergencyContainer.visibility = View.GONE
            }
            else{
                binding.llEmergencyContainer.visibility = View.VISIBLE
            }
//            binding.llEmergencyContainer.visibility = View.VISIBLE
        } else {
            binding.llEmergencyContainer.visibility = View.GONE
        }
        binding.rcyHistoryDataVoiceAndText.visibility = View.GONE
        binding.rlaBackRecord.visibility = View.GONE
        binding.lnrHistoryList.visibility = View.VISIBLE
        binding.rytVoiceRecord.visibility = View.GONE
        binding.lblDurationOfVoice.visibility = View.GONE
        binding.rlaAddLocalFile.visibility = View.GONE
        binding.imgVoiceRecord.setImageDrawable(
            ContextCompat.getDrawable(
                this@CommunicationSchool, R.drawable.record_icon
            )
        )
        mediaRecorder = null
        isRecording = false
        recordingHandler.removeCallbacks(recordingRunnable)
        Constant.selectedFiles.add(
            FileItem(path = data.url, type = FileType.AUDIO)
        )

        binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
        binding.edtTitle.setText("")
        binding.rlaTitle.visibility = View.VISIBLE
        binding.edtTitle.setText(data.title.toString())
        binding.lblEndDuration.text = "/ " + Constant.getAudioDurationInMinutes(data.url)
        Constant.isVoiceType = 3
        val voiceUrlOrPath = data.url
        audioFilePath = voiceUrlOrPath
        val currentDate: String = Constant.getCurrentDate()
        val isFileExtension = getFileExtensionFromAwsUrl(data.url)
        isFileName = Constant.sss_ + currentDate + "." + isFileExtension
        Log.d("RecordingFilePath", "Recording stopped. File Path: $audioFilePath")
    }

    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_AUDIO_REQUEST)
    }


    private fun isAllowedAudio(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        val name = getFileName(uri)?.lowercase() ?: ""

        return when {
            // WAV
            mimeType == "audio/wav" ||
                    mimeType == "audio/x-wav" -> true

            // M4A
            mimeType == "audio/mp4" -> true

            // MP3
            mimeType == "audio/mpeg" -> true

            // Fallback by extension
            name.endsWith(".wav") ||
                    name.endsWith(".m4a") ||
                    name.endsWith(".mp3") -> true

            else -> false
        }
    }




    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(
                    it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                )
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {

            val uri = data?.data ?: return

            val format = getPickedAudioFormat(uri)

            Log.d("PickedAudioFormat", "User selected audio format: $format")

            // 🔴 Validate format FIRST
            if (!isAllowedAudio(uri)) {
                Toast.makeText(
                    this,
                    "Only WAV, M4A or MP3 audio files are allowed",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(this, uri)
                mediaPlayer.prepare()

                val durationInMillis = mediaPlayer.duration
                val formattedDuration = formatDuration(durationInMillis)

                // ⏱ Duration validation (your logic)
                if (Constant.isCommunicationType != 2) {
                    if (binding.SwitchEmergencyVoice.isChecked()) {
                        if (durationInMillis > 30_000) {
                            mediaPlayer.release()
                            showDurationLimitDialog(getString(R.string.Audio_least_30_seconds))
                            return
                        }
                    } else {
                        if (durationInMillis > 180_000) {
                            mediaPlayer.release()
                            showDurationLimitDialog(getString(R.string.Audio_below_3_minutes))
                            return
                        }
                    }
                } else {
                    if (durationInMillis > 180_000) {
                        mediaPlayer.release()
                        showDurationLimitDialog(getString(R.string.Audio_below_3_minutes))
                        return
                    }
                }

                mediaPlayer.release()

                // 📄 Detect correct extension
                val extension = getAudioExtension(uri)

                val timeStamp = SimpleDateFormat(
                    Constant.yyyyMMdd_HHmmss,
                    Locale.getDefault()
                ).format(Date())

                val fileName = "${Constant.Communication_}${timeStamp}.$extension"
                isFileName = fileName

                // 📂 Copy to cache
                val inputStream = contentResolver.openInputStream(uri)
                val outputFile = File(cacheDir, fileName)
                val outputStream = FileOutputStream(outputFile)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                // Store local path
                audioFilePath = outputFile.absolutePath
                Constant.isVoiceType = 2
                Constant.selectedFiles!!.add(
                    FileItem(audioFilePath!!, FileType.AUDIO)
                )

                // 🖥 UI updates
                binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
                binding.edtTitle.setText("")
                binding.rlaTitle.visibility = View.VISIBLE
                binding.rytVoiceRecord.visibility = View.GONE
                binding.lblDurationOfVoice.visibility = View.GONE
                binding.rlaAddLocalFile.visibility = View.GONE
                binding.lblEndDuration.text = "/ $formattedDuration"

            } catch (e: Exception) {
                mediaPlayer.release()
                e.printStackTrace()
                Toast.makeText(
                    this,
                    getString(R.string.Failed_load_audio),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getPickedAudioFormat(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)

        return when (mimeType) {
            "audio/wav", "audio/x-wav" -> "WAV"
            "audio/mp4" -> "M4A"
            "audio/mpeg" -> "MP3"

            else -> {
                val name = getFileName(uri)?.lowercase()
                when {
                    name?.endsWith(".wav") == true -> "WAV"
                    name?.endsWith(".m4a") == true -> "M4A"
                    name?.endsWith(".mp3") == true -> "MP3"
                    else -> "UNKNOWN"
                }
            }
        }
    }


    private fun getAudioExtension(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)

        return when (mimeType) {
            "audio/wav", "audio/x-wav" -> "wav"
            "audio/mp4" -> "m4a"
            "audio/mpeg" -> "mp3"

            else -> {
                val name = getFileName(uri)?.lowercase()
                when {
                    name?.endsWith(".wav") == true -> "wav"
                    name?.endsWith(".m4a") == true -> "m4a"
                    name?.endsWith(".mp3") == true -> "mp3"
                    else -> ""
                }
            }
        }
    }

    private fun showDurationLimitDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.Invalid_Duration))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.permission_ok)) { dialog, _ ->
            dialog.dismiss()  // Dismiss the dialog when "OK" is clicked
        }
        builder.setCancelable(false)  // Make the dialog non-cancelable
        builder.show()
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

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun isScreenOff(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return !powerManager.isInteractive   // true = screen OFF
    }

    private fun isScreenLocked(): Boolean {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }


    override fun onStart() {
        super.onStart()

        when {
            isScreenOff() -> {
                binding.waveformSeekBar.updateWithLevel(0f)
                Log.d("ScreenState", "📴 Screen is OFF")
//                Toast.makeText(this, "Screen is OFF", Toast.LENGTH_SHORT).show()
            }

            isScreenLocked() -> {
                binding.waveformSeekBar.updateWithLevel(0f)
                Log.d("ScreenState", "🔒 Screen is LOCKED")
//                Toast.makeText(this, "Screen is LOCKED", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("ScreenState", "🔓 Screen is ON & UNLOCKED")
//                Toast.makeText(this, "Screen is ON & UNLOCKED", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onBackPressed() {
        if (mAdapter != null) {
            mAdapter!!.releaseMediaPlayer()
        }
        Constant.selectedFiles.clear()
//        Constant.isAwsUploadedFiles.clear()// Lastely added code 13 -01-2026

        if (binding.lnrHistoryList.isVisible == false) {
            binding.rytNORecordFound.visibility = View.GONE
            if (mAdapter != null) {
                mAdapter!!.releaseMediaPlayer()
            }
            Log.d("Constant.isCommunicationType", Constant.isCommunicationType.toString())
            when (Constant.isCommunicationType) {
                1 -> {
                    binding.gridViewScheduleCall.visibility = View.GONE
                    binding.rlaScheduleCallPickDate.visibility = View.GONE
                    binding.rlaRecordVoice.visibility = View.VISIBLE

                    if(isUserDetails!!.staff_role == Constant.isStaffRole || isUserDetails!!.staff_role == Constant.isNonTeachingStaffRole){
                        binding.llEmergencyContainer.visibility = View.GONE
                    }
                    else{
                        binding.llEmergencyContainer.visibility = View.VISIBLE
                    }
//                    binding.llEmergencyContainer.visibility = View.VISIBLE
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

        } else {
            super.onBackPressed()
        }
    }


}