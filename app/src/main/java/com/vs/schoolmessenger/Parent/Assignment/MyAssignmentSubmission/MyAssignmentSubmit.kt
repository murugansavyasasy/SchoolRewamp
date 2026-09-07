package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.isCommunicationType
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentSubmitBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAssignmentSubmit : BaseActivity<AssignmentSubmitBinding>(), View.OnClickListener,
    OnImageClickListener, VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): AssignmentSubmitBinding {
        return AssignmentSubmitBinding.inflate(layoutInflater)
    }

    var isTotalSelectedItem = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    private var mAdapter: ImagePickingAdapter? = null
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var cameraPermissionDeniedCount = 0
    private var pickImagesLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickVideoLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    var assignmentId: String? = null
    var titleName: String? = null
    var subjectName: String? = null
    var submissionData: SubmittedAssignment? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        Constant.Remaining = MAX_FILES


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.btnChooseRecipient.setOnClickListener(this)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails!!.access_token
        assignmentId = intent.getStringExtra(Constant.assignment_id)
        titleName = intent.getStringExtra(Constant.title_)
        subjectName = intent.getStringExtra(Constant.subject)
        submissionData = intent.getParcelableExtra(Constant.mysubmission_data)

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        if (submissionData != null) {
            binding.toolbarLayout.lblParentToolBar.text = getString(R.string.edit_your_assignment)
        } else {
            binding.toolbarLayout.lblParentToolBar.text = getString(R.string.submit_your_assignment)
        }
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }


        pickImagesLauncher =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(Constant.isFilesAllow)
            ) { uris ->

                if (uris.isEmpty()) return@registerForActivityResult

                if (uris.size > Constant.isFilesAllow) {
                    Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                }

                val limitedUris = uris.take(Constant.isFilesAllow)

                handleSelectedImages(limitedUris, Constant.IMAGE)
            }

        pickVideoLauncher =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(Constant.isVideoAllow)
            ) { uris ->

                if (uris.isEmpty()) return@registerForActivityResult

                if (uris.size > Constant.isVideoAllow) {
                    Toast.makeText(this, "Only 2 videos allowed", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                handleSelectedImages(uris, Constant.VIDEO)
            }


        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        binding.edtTitle.setText(titleName)

        selectedFiles.clear()
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        if (submissionData != null) {
            binding.edtTitle.setText(submissionData!!.title)
            binding.edtDescription.setText(submissionData!!.description)
            for (file in submissionData!!.file_path) {
                val type = when (file.type) {
                    "IMAGE" -> FileType.IMAGE
                    "VIDEO" -> FileType.VIDEO
                    "AUDIO" -> FileType.AUDIO
                    "PDF" -> FileType.PDF
                    "DOC" -> FileType.DOC
                    "EXCEL" -> FileType.EXCEL
                    "PPT" -> FileType.PPT
                    "TXT" -> FileType.TXT
                    else -> FileType.OTHER
                }
                selectedFiles.add(FileItem(file.url, type))
            }
            Constant.Remaining = MAX_FILES - submissionData!!.file_path.size
        }

        appViewModel!!.isSubmitAssignment?.observe(this) { response ->
            Constant.hideLoading(this@MyAssignmentSubmit)
            if (response != null) {
                Log.d("Response", response.status.toString())
                if (response.status == true) {
                    showTopAlertPopup(response.message, this)
                } else {
                    Constant.showTopAlertPopup(response.message, this)
                }
            }
        }

        appViewModel!!.getmysubmissionedit?.observe(this) { response ->
            Constant.hideLoading(this@MyAssignmentSubmit)
            if (response != null) {
                Log.d("Response", response.status.toString())
                if (response.status == true) {
                    showTopAlertPopup(response.message, this)
                } else {
                    showTopAlertPopup(response.message, this)
                }
            }
        }


        mAdapter = ImagePickingAdapter(this, selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter

    }
    private fun handleSelectedImages(uris: List<Uri>, fileType: String) {

        if (uris.isEmpty()) return

        if (uris.size > Constant.isFileLimit) {
            Toast.makeText(
                this,
                "You can select only $Constant.isFileLimit files",
                Toast.LENGTH_SHORT
            ).show()
        }

        Log.d("urisReturn", uris.size.toString())

        val finalFiles = uris.take(Constant.isFileLimit)

        if (Constant.Remaining > 0 && finalFiles.isNotEmpty()) {

            val previousCount = Constant.selectedFiles.size
            Constant.Remaining -= finalFiles.size

            Log.d("Constant.Remaining", Constant.Remaining.toString())
            Log.d("Constant.Remaining", finalFiles.size.toString())

            finalFiles.forEach { uri ->

                val mimeType = contentResolver.getType(uri)

                val path = when (uri.scheme) {
                    Constant.file_ -> uri.path
                    else -> getPathFromUri(uri)
                }

                if (path == null) {
                    Log.w("addPath", "Could not resolve path from URI: $uri")
                    return@forEach
                }

                val fileName = getFileName(uri).takeIf { it.isNotEmpty() }
                    ?: uri.lastPathSegment?.substringAfterLast("/")
                    ?: "temp_file_${System.currentTimeMillis()}"

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

                Log.d("MAX_FILES", MAX_FILES.toString())

                if (type == FileType.VIDEO) {

                    val videoCount = Constant.selectedFiles.count {
                        it.type == FileType.VIDEO
                    }

                    if (videoCount >= 2) {
                        Toast.makeText(
                            this,
                            "Only 2 videos are allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@forEach
                    }
                }

                if (Constant.selectedFiles.size < MAX_FILES + 1) {
                    Constant.selectedFiles.add(FileItem(uri.toString(), type))
                } else {
                    Constant.Remaining = 0
                }

                Log.d("SelectedFile", "URI: $uri, Type: $type")
            }

            mAdapter?.notifyDataSetChanged()

            val addedCount = Constant.selectedFiles.size - previousCount
            val totalCount = Constant.selectedFiles.size

            Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")

        } else if (Constant.Remaining <= 0) {
            Log.d("isComing", "Limit reached")
        }
    }


    fun showTopAlertPopup(message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            isCommunicationType = 1
            val intent = Intent(activity, Assignment::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true

    }

    private fun getPathFromUri(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "temp_file"
            val file = File(cacheDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null

        if (uri.scheme.equals("content", ignoreCase = true)) {
            val projection = arrayOf(OpenableColumns.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        fileName = cursor.getString(columnIndex)
                    }
                }
            }
        }

        if (fileName.isNullOrEmpty()) {
            fileName = uri.lastPathSegment
            fileName = fileName?.substringAfterLast("/")
        }

        return fileName ?: "temp_file_${System.currentTimeMillis()}"
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.ENGLISH).format(Date())

        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("${Constant.IMG_}${timeStamp}_", Constant.jpg, storageDir)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnChooseRecipient -> {
                showSendConfirmationDialog()
            }
        }
    }


    fun showSendConfirmationDialog() {
        // Validate description first
        val descriptionText = binding.edtDescription.text.toString().trim()
        if (descriptionText.isEmpty()) {
            binding.edtDescription.error = getString(R.string.This_field_required)
            binding.edtDescription.requestFocus()
            return
        }

        if (submissionData != null) {
            val currentTitle = binding.edtTitle.text.toString().trim()
            val currentDesc = descriptionText
            val originalTitle = submissionData!!.title?.trim() ?: ""
            val originalDesc = submissionData!!.description?.trim() ?: ""
            if (currentTitle == originalTitle && currentDesc == originalDesc) {
                val currentFileSet =
                    selectedFiles.drop(1).map { it.path to it.type.name }.toSet()
                val originalFileSet = submissionData!!.file_path.map { it.url to it.type }.toSet()
                if (currentFileSet == originalFileSet) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.no_changes))
                        .setMessage(getString(R.string.no_changes_have_been_detected))
                        .setPositiveButton(getString(R.string.OK_2), null)
                        .show()
                    return
                }
            }
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        if (submissionData != null) {
            alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_assignment)
        } else {
            alertMessage.text = getString(R.string.Are_you_sure_want_to_submit)
        }

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            ProgressDialogHelper.show(this)
            isUploadFilesInServer(Constant.file_)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    fun isUploadFilesInServer(isFileType: String?) {

        selectedFiles.removeAt(0)
        isTotalSelectedItem = selectedFiles.size
        isVideoSelectedArrayList.clear()
        isAwsUploadedFiles.clear()
        val iterator = selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.type == FileType.VIDEO) {
                isVideoSelectedArrayList.add(file)
                iterator.remove()
            }
        }

        val numNonVideoFiles = selectedFiles.size
        val numVideos = isVideoSelectedArrayList.size

        val videoSteps = 10
        var totalTasks = (numNonVideoFiles * 2) + (numVideos * videoSteps)

        if (totalTasks == 0 && numVideos > 0) {
            totalTasks = videoSteps
        }
        var completedTasks = 0

        fun updateProgress() {
            if (totalTasks > 0) {
                val progress = (completedTasks * 100) / totalTasks
                ProgressDialogHelper.updateProgress(progress)
            } else {
                ProgressDialogHelper.dismiss()
            }
        }

        when {
            selectedFiles.isNotEmpty() -> isFileUploadInAws(
                isFileType,
                totalTasks,
                { completedTasks++; updateProgress() })

            isVideoSelectedArrayList.isNotEmpty() -> videoUploading(
                totalTasks,
                { completedTasks++; updateProgress() })

            else -> {
                ProgressDialogHelper.dismiss()
                isAssignmentSend()
            }
        }
    }

    private fun isFileUploadInAws(
        isFileType: String?,
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        val iterator = selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path,
                        isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
                isAssignmentSend()
            } else {
                videoUploading(totalTasks, onTaskComplete)
            }
        } else {
            selectedFiles.size
            val outputDir =
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
            outputDir.mkdirs()
            val newSelectedFiles = mutableListOf<FileItem>()
            Constant.compressImageFilesOnly(
                context = this,
                files = selectedFiles,
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
                    onTaskComplete()
                },
                onComplete = {
                    selectedFiles.clear()
                    selectedFiles.addAll(newSelectedFiles)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = selectedFiles.size
                    for (i in selectedFiles.indices) {
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            selectedFiles[i].path,
                            isChildDetails!!.school_id,
                            isFileType!!,
                            this@MyAssignmentSubmit,
                            isCountryId!!,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?,
                                    isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = selectedFiles.getOrNull(i)?.type?.name
                                                ?: "UNKNOWN"
                                        )
                                    )
                                    onTaskComplete()

                                    if (isTotalSelectedItem == isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
                                        isAssignmentSend()
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading(totalTasks, onTaskComplete)
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    onTaskComplete()
                                    Log.d("isUploadIssue", error.toString())
                                }
                            })
                    }

                    Log.d("Compressor", "All files compressed and uploaded.")
                })
        }
    }

    private fun videoUploading(
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("player.vimeo.com")) {
                isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path,
                        isFileType = fileItem.type.name
                    )
                )

                iterator.remove()
            }
        }
        Log.d("isVideoSelectedArrayList", isVideoSelectedArrayList.size.toString())
        if (isVideoSelectedArrayList.isNotEmpty()) {
            for (i in isVideoSelectedArrayList.indices) {
                Thread {
                    for (x in 1..10) {
                        Thread.sleep(400)
                        runOnUiThread { onTaskComplete() }
                    }
                }.start()

                VimeoVideoUpload.uploadVideo(
                    this, Constant.quiz, Constant.quiz, isVideoSelectedArrayList[i].path, this
                )
            }
        } else {
            ProgressDialogHelper.dismiss()
            isAssignmentSend()
        }
    }

    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        runOnUiThread {
            Log.d("link", link.toString())
            isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (isAwsUploadedFiles.size == isTotalSelectedItem) {
                ProgressDialogHelper.dismiss()
                isAssignmentSend()
            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")

            ProgressDialogHelper.dismiss()
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {
            // Show rationale if user has denied permission before
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
                    Toast.makeText(
                        this,
                        getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT
                    ).show()
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

    private fun openAlbumSelectActivity(isFileType: String) {
        Log.d("FileComing", isFileType)
        if (isFileType == Constant.DOCUMENT) {
            openSystemDocumentPicker()
        }
        else if(isFileType == Constant.VIDEO){
            pickVideoLauncher!!.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
        else if(isFileType == Constant.IMAGE) {
            pickImagesLauncher!!.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }
    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST)
    }

    override fun onBackPressed() {
        selectedFiles.clear()
        isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
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

        rlaVideoPick.setOnClickListener {
            val selectedVideoCount = selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(
                    this,
                    getString(R.string.only_2_videos_are_allowed), Toast.LENGTH_SHORT
                ).show()
            } else {
                if (selectedFiles.size == 1 || selectedVideoCount == 0) {
                    Constant.isFileLimit = 10
                } else if (selectedVideoCount == 1) {
                    Constant.isFileLimit = 10
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


    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        if (photoFile == null) {
            Toast.makeText(
                this,
                getString(R.string.could_not_create_file_for_photo),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val photoURI = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        cameraImageFilePath = photoFile.absolutePath

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Camera not available on this device",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("CameraError", "Camera launch failed", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        if (Constant.Remaining!! == 0) {
            Toast.makeText(
                this,
                "${getString(R.string.Max)} ${MAX_FILES} ${getString(R.string.files_allowed)}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        fun addPath(uri: Uri) {
            Log.d("isFilePickingUrl", uri.toString())

            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) {
                Log.d("SkipFile", "Skipping audio/video file: $uri (MIME: $mimeType)")
                return
            }

            val fileName = getFileName(uri)
            val safeMime = mimeType ?: ""

            val type = when {

                // ✅ PDF
                safeMime == "application/pdf" ||
                        fileName.endsWith(".pdf", true) ->
                    FileType.PDF

                // ✅ WORD
                safeMime == "application/msword" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) ->
                    FileType.DOC

                // ✅ EXCEL
                safeMime == "application/vnd.ms-excel" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                        fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) ->
                    FileType.EXCEL

                // ✅ POWERPOINT
                safeMime == "application/vnd.ms-powerpoint" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.presentationml.presentation" ||
                        fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) ->
                    FileType.PPT

                // ✅ TEXT
                safeMime == "text/plain" ||
                        fileName.endsWith(".txt", true) ->
                    FileType.TXT

                // ✅ IMAGE
                safeMime.startsWith("image/") ||
                        fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) ->
                    FileType.IMAGE

                else -> FileType.OTHER
            }
            if (selectedFiles.size < MAX_FILES + 1) {
                selectedFiles.add(FileItem(uri.toString(), type))
            } else {
                Constant.Remaining = 0
            }
            for (item in selectedFiles) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
            }
        }

        when (requestCode) {
            CAMERA_IMAGE_REQUEST -> {
                cameraImageFilePath?.let { filePath ->
                    var file = File(filePath)
                    if (file.exists()) {
                        if (!file.name.endsWith(".jpg", true)) {
                            val newFile = File(file.parent, file.nameWithoutExtension + ".jpg")
                            if (file.renameTo(newFile)) {
                                cameraImageFilePath = newFile.absolutePath
                                file = newFile
                            }
                        }
                        val uri = Uri.fromFile(file)
                        Constant.Remaining = Constant.Remaining - 1

                        addPath(uri)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.camera_image_file_not_found), Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } ?: run {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_image_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            PICK_DOCUMENT_REQUEST -> {
                val clipData = data?.clipData
                val singleUri = data?.data

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        addPath(uri)
                    }
                    Constant.Remaining = Constant.Remaining - clipData.itemCount

                } else if (singleUri != null) {
                    addPath(singleUri)
                    Constant.Remaining = Constant.Remaining - 1

                }
            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    fun isAssignmentSend() {
        ProgressDialogHelper.dismiss()

        val id = if (submissionData != null) submissionData!!.id else assignmentId!!
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", id)
        jsonObject.addProperty("description", binding.edtDescription.text.toString())
        jsonObject.addProperty("iframe", "")
        jsonObject.addProperty("file_size", isAwsUploadedFiles.size.toString())
        val filePathArray = JsonArray()
        for (file in isAwsUploadedFiles) {
            val obj = JsonObject()
            obj.addProperty("url", file.isFileUrl)
            obj.addProperty("type", file.isFileType)
            filePathArray.add(obj)
        }
        jsonObject.add("file_path", filePathArray)
        if (submissionData != null) {
            appViewModel!!.getmysubmissionedit(isAccessToken!!, jsonObject, this)
        } else {
            appViewModel!!.isSubmitAssignment(isAccessToken!!, jsonObject, this)
        }
    }
}