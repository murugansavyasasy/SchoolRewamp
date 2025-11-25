package com.vs.schoolmessenger.School.LSRW

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.LSRW.Adapter.LSRWImagePickingAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwnewTaskSendingData
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CreateNewtaskLsrwBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.endsWith
import kotlin.text.ifEmpty
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class CreateNewTask : BaseActivity<CreateNewtaskLsrwBinding>(), View.OnClickListener,
    OnDateSelectedListener, OnImageClickListener, VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): CreateNewtaskLsrwBinding {
        return CreateNewtaskLsrwBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isSelectedDate = ""
    var isCreateNewTaskPosition = 0
    var isTotalSelectedItem = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        const val MAX_FILES = 10
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: LSRWImagePickingAdapter? = null
    private var selectedSkill: String = Constant.Listening
    private lateinit var tabList: List<LinearLayout>
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFilePath: String? = null
    private var isRecording = false
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 201
    private var audioPermissionDeniedCount = 0

    private fun updateRemainingCount() {
        val usedSlots = Constant.selectedFiles.size - 1
        Constant.Remaining = (MAX_FILES - usedSlots).coerceAtLeast(0)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        Constant.Remaining = MAX_FILES

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.edtdate.setOnClickListener {
            if (isSelectedDate.isNotEmpty()) {
                lsrwshowDatePickerDialog(this, this, isSelectedDate)
            } else {
                lsrwshowDatePickerDialog(this, this)
            }
        }

        binding.edtDescription.apply {
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_ALWAYS
            setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }

        binding.btnChooseRecipient.setOnClickListener(this)

        tabList = listOf(
            binding.listeningLayout,
            binding.speakingLayout,
            binding.readingLayout,
            binding.writingLayout
        )



        tabList.forEach { layout ->
            layout.setOnClickListener { setSelectedTab(layout) }
        }

        setSelectedTab(binding.listeningLayout)

        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(FileItem(it, FileType.IMAGE))
        }

        binding.rcyImages.visibility = View.VISIBLE

        mAdapter = LSRWImagePickingAdapter(this, Constant.selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 1)
        binding.rcyImages.adapter = mAdapter


        // Update remaining after adding placeholder
        updateRemainingCount()

        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris = result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                    if (selectedUris.isNullOrEmpty()) return@registerForActivityResult

                    var addedCount = 0
                    selectedUris.forEach { uri ->
                        if (Constant.selectedFiles.size >= MAX_FILES + 1) {
                            Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
                            return@forEach
                        }

                        val mimeType = contentResolver.getType(uri)
                        val path = when (uri.scheme) {
                            Constant.file_ -> uri.path
                            else -> getPathFromUri(uri)
                        } ?: run {
                            Log.w("addPath", "Could not resolve path from URI: $uri")
                            return@forEach
                        }

                        val fileName = getFileName(uri).ifEmpty { File(path).name }
                        val type = when {
                            mimeType?.startsWith("image/") == true -> FileType.IMAGE
                            mimeType?.startsWith("video/") == true -> FileType.VIDEO
                            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                            fileName.endsWith(".pdf", true) -> FileType.PDF
                            fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                            fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
                            fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                            fileName.endsWith(".txt", true) -> FileType.TXT
                            else -> FileType.OTHER
                        }

                        if (type == FileType.AUDIO) {
                            lifecycleScope.launch {
                                val wavFile = Constant.convertToWav(this@CreateNewTask, uri)
                                if (wavFile != null) {
                                    Constant.selectedFiles.add(FileItem(wavFile.absolutePath, FileType.AUDIO))
                                    mAdapter?.notifyDataSetChanged()
                                    updateRemainingCount()
                                } else {
                                    Toast.makeText(this@CreateNewTask, "Audio convert failed!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            return@forEach
                        }


                        Constant.selectedFiles.add(FileItem(uri.toString(), type))
                        addedCount++
                    }

                    if (addedCount > 0) {
                        mAdapter!!.notifyDataSetChanged()
                        updateRemainingCount()

                        if (addedCount < selectedUris.size) {
                            Toast.makeText(this, getString(R.string.only_x_files_added, Constant.Remaining), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.btnChooseRecipient -> isRedirectToSectionStudents()
        }
    }


    override fun onDateSelected(date: String) {
        isSelectedDate = date
        binding.edtdate.text = Constant.convertToReadableDate(date)
    }


    private fun setSelectedTab(selected: LinearLayout) {
        tabList.forEach { it.setBackgroundResource(R.drawable.btn_unselected) }
        selected.setBackgroundResource(R.drawable.btn_selected)
        selectedSkill = when (selected.id) {
            binding.listeningLayout.id -> Constant.Listening
            binding.speakingLayout.id -> Constant.Speaking
            binding.readingLayout.id -> Constant.Reading
            binding.writingLayout.id -> Constant.Writing
            else -> Constant.Listening
        }
    }




    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {

            if (cameraPermissionDeniedCount >= 2 && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA
                )
            ) {
                showCameraPermissionSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun checkRecordPermissionAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceRecording()
        } else {
            if (audioPermissionDeniedCount >= 2 && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )
            ) {
                showAudioPermissionSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraIntent()
            } else {
                cameraPermissionDeniedCount++
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.CAMERA
                    )
                ) {
                    showCameraPermissionSettingsDialog()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecording()
            } else {
                audioPermissionDeniedCount++
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    showAudioPermissionSettingsDialog()
                } else {
                    Toast.makeText(this, getString(R.string.microphone_permission_is_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.camera_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false).setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showAudioPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.microphone_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false).setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun startVoiceRecording() {
        if (isRecording) return

        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir("recordings") ?: cacheDir
        val audioFile: File = try {
            File.createTempFile("AUDIO_${timeStamp}_", ".wav", storageDir)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(
                this,
                getString(R.string.could_not_create_file_for_audio),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        recordingFilePath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFilePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
                releaseRecorder()
                Toast.makeText(
                    this@CreateNewTask,
                    getString(R.string.failed_to_start_recording),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Replace your old dialog code with this:
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_recording, null)
        val stopBtn = dialogView.findViewById<TextView>(R.id.btnStop)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        stopBtn.setOnClickListener {
            stopVoiceRecording()
            dialog.dismiss()
        }
    }


    private fun stopVoiceRecording() {
        if (!isRecording) return
        isRecording = false
        try {
            mediaRecorder?.stop()
        } catch (e: RuntimeException) { }
        mediaRecorder?.release()
        mediaRecorder = null

        recordingFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                if (Constant.selectedFiles.size < MAX_FILES + 1) {
                    Constant.selectedFiles.add(FileItem(path, FileType.AUDIO))
                    mAdapter?.notifyDataSetChanged()
                    updateRemainingCount()
                    Toast.makeText(this, getString(R.string.audio_recorded_and_added), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
                    file.delete()
                }
            } else {
                Toast.makeText(this, getString(R.string.recording_failed_file_empty), Toast.LENGTH_SHORT).show()
                file.delete()
            }
        }
        recordingFilePath = null
    }

    private fun releaseRecorder() {
        if (isRecording) {
            stopVoiceRecording()
        } else {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun openAlbumSelectActivity(isFileType: String) {

        Log.d("FileComing", isFileType)
        val sdkInt = Build.VERSION.SDK_INT
        if (isFileType == Constant.DOCUMENT && sdkInt < Build.VERSION_CODES.R) {
            openSystemDocumentPicker()
        } else {
            val intent = Intent(this, AlbumSelectActivity::class.java)
            intent.putExtra(Constant.isFileType, isFileType)
            albumResultLauncher.launch(intent)
        }
    }

    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, CreateNewTask.Companion.PICK_DOCUMENT_REQUEST)
    }

    override fun onBackPressed() {
        if (isRecording) {
            stopVoiceRecording()
        }
        mAdapter?.releaseMediaPlayer()
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        updateRemainingCount()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopVoiceRecording()
        }
        mAdapter?.releaseMediaPlayer()
        Constant.stopDelay()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRecorder()
        mAdapter?.releaseMediaPlayer()
    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)

        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)
        val rlaVoice = dialog.findViewById<RelativeLayout>(R.id.rlaVoice)
        val rlaVideoPick = dialog.findViewById<RelativeLayout>(R.id.rlaVideoPick)
        val rlavoicerecorder = dialog.findViewById<RelativeLayout>(R.id.rlavoicerecorder)

        rlaVoice.visibility = View.VISIBLE
        rlavoicerecorder.visibility = View.VISIBLE

        rlaGallery.setOnClickListener {
            Constant.isFileLimit = 10
            Log.d("Constant.isFileLimit", Constant.isFileLimit.toString())

            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlavoicerecorder.setOnClickListener {
            Constant.isFileLimit = 10
            openVoiceRecorder()
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            val selectedVideoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(this, getString(R.string.only_2_videos_are_allowed), Toast.LENGTH_SHORT).show()
            } else {
                if (Constant.selectedFiles.size == 1 || selectedVideoCount == 0) {
                    Constant.isFileLimit = 2
                } else if (selectedVideoCount == 1) {
                    Constant.isFileLimit = 1
                }
                openAlbumSelectActivity(Constant.VIDEO)
                dialog.dismiss()
            }
        }

        rlaDocument.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.DOCUMENT)
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
            dialog.dismiss()
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(R.style.PopupAnimation)
        }
        dialog.show()
    }


    private fun openVoiceRecorder() {
        checkRecordPermissionAndStartRecording()
    }

    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this, "${applicationContext.packageName}.fileprovider", photoFile
                )
                cameraImageFilePath = photoFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CreateEvent.Companion.CAMERA_IMAGE_REQUEST)
            } else {
                Toast.makeText(this, getString(R.string.could_not_create_file_for_photo), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || Constant.selectedFiles.size >= MAX_FILES + 1) {
            if (Constant.selectedFiles.size >= MAX_FILES + 1) {
                Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
            }
            return
        }

        fun addFile(uri: Uri) {
            if (Constant.selectedFiles.size >= MAX_FILES + 1) return

            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) return

            val fileName = getFileName(uri)
            val type = when {
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            Constant.selectedFiles.add(FileItem(uri.toString(), type))
        }

        when (requestCode) {
            CAMERA_IMAGE_REQUEST -> {
                cameraImageFilePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val finalFile = if (!file.name.endsWith(".jpg", true)) {
                            val newFile = File(file.parent, file.nameWithoutExtension + ".jpg")
                            file.renameTo(newFile)
                            newFile
                        } else file
                        addFile(Uri.fromFile(finalFile))
                    }
                }
            }
            PICK_DOCUMENT_REQUEST -> {
                data?.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        addFile(clip.getItemAt(i).uri)
                    }
                } ?: data?.data?.let { addFile(it) }
            }
        }

        mAdapter?.notifyDataSetChanged()
        updateRemainingCount()
    }

    private fun getPathFromUri(uri: Uri): String? {
        // Content scheme
        if (uri.scheme.equals(Constant.content_, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(columnIndex)
                }
            }
        }

        // File scheme fallback
        if (uri.scheme.equals(Constant.file_, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == Constant.content_) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: ""
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir)
    }

    private fun isRedirectToSectionStudents() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val edtdate = binding.edtdate.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.shake()
            Toast.makeText(this, "Please select title", Toast.LENGTH_SHORT).show()
            return
        }

        if (edtdate.isEmpty()) {
            binding.edtdate.shake()
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            binding.edtDescription.shake()
            Toast.makeText(this, "Please select description", Toast.LENGTH_SHORT).show()
            return
        }


        val isLsrwnewTaskSendingData = LsrwnewTaskSendingData(
            title,
            description,
            selectedSkill,
            edtdate,
        )
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.lsrwskill_data, isLsrwnewTaskSendingData)
        startActivity(intent)
    }


    fun View.shake() {
        val anim = AnimationUtils.loadAnimation(context, R.anim.shake)
        startAnimation(anim)
    }

    fun isUploadFilesInServer(isFileType: String?) {
        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_NOTICEBOARD || SELECTED_MENU_ID == M_LSRW) {
            Constant.selectedFiles.removeAt(0) // Remove '+' placeholder
        }
        ProgressDialogHelper.updateProgress(50)
        isTotalSelectedItem = Constant.selectedFiles.size
        isVideoSelectedArrayList.clear()
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.type == FileType.VIDEO) {
                isVideoSelectedArrayList.add(file)
                iterator.remove()
            }
        }

        when {
            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(isFileType)
            isVideoSelectedArrayList.isNotEmpty() -> videoUploading()
        }
        ProgressDialogHelper.updateProgress(80)
    }


    private fun isFileUploadInAws(
        isFileType: String?
    ) {
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (Constant.selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
                //   isUpdateEvent()
            } else {
                videoUploading()
            }
        } else {
            val outputDir =
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
            outputDir.mkdirs()
            val newSelectedFiles = mutableListOf<FileItem>()
            Constant.compressImageFilesOnly(
                context = this,
                files = Constant.selectedFiles,
                outputDir = outputDir.absolutePath,
                format = Bitmap.CompressFormat.JPEG,
                quality = 80,
                maxWidth = 1280,
                maxHeight = 1280,
                onEachProcessed = { original, outputPath, success ->
                    if (success && outputPath != null) {
                        val compressedFile = File(outputPath)
                        val originalSizeKB = try {
                            if (original.path.startsWith("content://")) {
                                contentResolver.openFileDescriptor(
                                    Uri.parse(original.path), "r"
                                )?.statSize ?: 0
                            } else {
                                File(original.path).length()
                            }
                        } catch (e: Exception) {
                            0L
                        }

                        Log.d(
                            "Compressor",
                            "Compressed: $outputPath (${compressedFile.length() / 1024}KB), Original: ${originalSizeKB / 1024}KB"
                        )

                        newSelectedFiles.add(FileItem(path = outputPath, type = original.type))
                    } else {
                        Log.e("Compressor", "Failed: ${original.path}")
                    }
                },
                onComplete = {
                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(newSelectedFiles)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = Constant.selectedFiles.size
                    for (i in Constant.selectedFiles.indices) {
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            Constant.selectedFiles[i].path,
                            isStaffDetails!!.school_id,
                            isFileType!!,
                            this,
                            isCountryId!!,
                            true,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?, isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    Constant.isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = Constant.selectedFiles[i].type.name
                                        )
                                    )

                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
                                        //   isUpdateEvent()
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading()
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    Log.d("isUploadIssue", error.toString())
                                }
                            })
                    }

                    Log.d("Compressor", "All files compressed and uploaded.")
                })
        }
    }

    private fun videoUploading() {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("player.vimeo.com")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }
        Log.d("isVideoSelectedArrayList", isVideoSelectedArrayList.size.toString())
        if (isVideoSelectedArrayList.isNotEmpty()) {
            for (i in isVideoSelectedArrayList.indices) {
                VimeoVideoUpload.uploadVideo(
                    this, "quiz", "quiz", isVideoSelectedArrayList[i].path, this
                )
            }
        } else {
            ProgressDialogHelper.dismiss()

        }
    }

    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        runOnUiThread {
            Log.d("link", link.toString())
            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
                ProgressDialogHelper.dismiss()

            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }
}