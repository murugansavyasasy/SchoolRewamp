package com.vs.schoolmessenger.Parent.RaiseConcern

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RaiseConcernBinding
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Parent.RaiseConcern.ConcernTypeModel.ConcernType
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ParentConcern
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RaiseConcernActivity :
    BaseActivity<RaiseConcernBinding>(),
    View.OnClickListener,
    OnImageClickListener, VimeoVideoUpload.UploadCompletionListener  {

    override fun getViewBinding(): RaiseConcernBinding {
        return RaiseConcernBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null
    var userDetails: UserDetails? = null

    private var concernTypeList: List<ConcernType> = emptyList()
    private var selectedConcernTypeId: String = ""
    private var selectedRaisedTo: String = ""

    private var pendingDescription: String = ""

    private var concernList: MutableList<ParentConcern> = mutableListOf()
    private lateinit var concernAdapter: ParentConcernAdapter

    var isTotalSelectedItem = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()

    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var cameraPermissionDeniedCount = 0
    private var pickImagesLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickVideoLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var mAdapter: ImagePickingAdapter? = null

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails!!.access_token

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        Constant.SELECTED_MENU_ID = Constant.M_ATTACHMENTS


        binding.toolbarLayout.apply {
            imgBack.setOnClickListener(this@RaiseConcernActivity)
            lblStudentName.text = isChildDetails?.name
            lblStudentSection.text =
                "${isChildDetails?.standard_name} - ${isChildDetails?.section_name}"

            imgSearchToolBar.setOnClickListener {
                if (binding.rytSearch1.visibility == View.VISIBLE) {
                    binding.rytSearch1.visibility = View.GONE
                } else {
                    binding.rytSearch1.visibility = View.VISIBLE
                    binding.txtVideoMenu1.text.clear()
                }
            }
        }

        binding.tabOneName.text = "Raise concern"
        binding.tabTwoName.text = "Raised concern list"

        setupTabClicks()
        setupConcernListRecycler()
        setupRadioGroup()
        setupSubmitButton()

        showRaiseConcernTab()

        callConcernTypeApi()

        appViewModel?.isParentConcernlist?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this)
                if (response.status && !response.data.isNullOrEmpty()) {
                    val data = response.data ?: emptyList()
                    concernList.clear()
                    concernList.addAll(data)
                    concernAdapter.updateList(concernList)
                    toggleEmptyState(concernList.isEmpty())
                } else {
                    toggleEmptyState(true)
                }
            }
        }

        appViewModel?.isRaiseParentConcern?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this)
                if (response.status) {
                    Toast.makeText(
                        this@RaiseConcernActivity,
                        response.message ?: "Concern raised successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearForm()
                    showConcernListTab()
                } else {
                    Toast.makeText(
                        this@RaiseConcernActivity,
                        response.message ?: "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        appViewModel?.isRaiseConcernType?.observe(this) { response ->
            if (response != null) {
                if (response.status && !response.data.isNullOrEmpty()) {
                    concernTypeList = response.data ?: emptyList()
                    setupConcernTypeSpinner()
                } else {
                    Log.e("RaiseConcern", "concernType error: ${response.message}")
                }
            }
        }

        appViewModel?.isRemoveConcern?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this)
                if (response.status) {
                    Toast.makeText(
                        this@RaiseConcernActivity,
                        response.message ?: "Concern deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    callParentConcernListApi()
                } else {
                    Toast.makeText(
                        this@RaiseConcernActivity,
                        response.message ?: "Failed to delete concern",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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

        selectedFiles.clear()
        isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES
        renderDrawableToCacheFile(R.drawable.attachment_with_bg)?.let {
            selectedFiles.add(FileItem(it, FileType.IMAGE))
        }

        mAdapter = ImagePickingAdapter(this, selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter
    }


    private fun renderDrawableToCacheFile(drawableResId: Int): String? {
        return try {
            val drawable = ContextCompat.getDrawable(this, drawableResId) ?: return null
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            val file = File(cacheDir, "attachment_placeholder_${System.currentTimeMillis()}.png")
            file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleSelectedImages(uris: List<Uri>, fileType: String) {
        if (uris.isEmpty()) return

        if (uris.size > Constant.isFileLimit) {
            Toast.makeText(
                this,
                "You can select only ${Constant.isFileLimit} files",
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

                if (type == FileType.VIDEO) {
                    val videoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
                    if (videoCount >= 2) {
                        Toast.makeText(this, "Only 2 videos are allowed", Toast.LENGTH_SHORT).show()
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

    private fun getPathFromUri(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri)
            val file = File(cacheDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
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
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())

        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("${Constant.IMG_}${timeStamp}_", Constant.jpg, storageDir)
    }

    private fun setupTabClicks() {
        binding.lnrTabOneName.setOnClickListener { showRaiseConcernTab() }
        binding.lnrTabTwoName.setOnClickListener { showConcernListTab() }
    }

    private fun showRaiseConcernTab() {
        binding.lytRaiseConcern.visibility = View.VISIBLE
        binding.lytConcernList.visibility = View.GONE

        binding.tabOneName.setTextColor(resources.getColor(R.color.PrimaryColor))
        binding.tabTwoName.setTextColor(resources.getColor(R.color.black))
        binding.line1.setBackgroundColor(resources.getColor(R.color.PrimaryColor))
        binding.line2.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    private fun showConcernListTab() {
        binding.lytRaiseConcern.visibility = View.GONE
        binding.lytConcernList.visibility = View.VISIBLE

        binding.tabTwoName.setTextColor(resources.getColor(R.color.PrimaryColor))
        binding.tabOneName.setTextColor(resources.getColor(R.color.black))
        binding.line2.setBackgroundColor(resources.getColor(R.color.PrimaryColor))
        binding.line1.setBackgroundColor(resources.getColor(android.R.color.transparent))
        callParentConcernListApi()
    }


    private fun setupRadioGroup() {
        val radioButtons = listOf(binding.rbManagement, binding.rbPrincipal, binding.rbClassTeacher)

        val onRadioClick = View.OnClickListener { clicked ->
            radioButtons.forEach { rb -> rb.isChecked = (rb.id == clicked.id) }
            selectedRaisedTo = when (clicked.id) {
                R.id.rbManagement -> "management"
                R.id.rbPrincipal -> "principle"
                R.id.rbClassTeacher -> "class_teacher"
                else -> ""
            }
        }

        radioButtons.forEach { it.setOnClickListener(onRadioClick) }
    }

    private fun callConcernTypeApi() {
        appViewModel!!.isRaiseConcernType(isAccessToken!!, this)
    }

    private fun setupConcernTypeSpinner() {
        val names = mutableListOf("Select concern type")
        names.addAll(concernTypeList.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerConcernType.adapter = adapter

        binding.spinnerConcernType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    selectedConcernTypeId =
                        if (position == 0) "" else concernTypeList[position - 1].id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedConcernTypeId = ""
                }
            }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val description = binding.edtDescription.text.toString().trim()

            when {
                selectedConcernTypeId.isEmpty() ->
                    Toast.makeText(this, "Please select a concern type", Toast.LENGTH_SHORT).show()

                selectedRaisedTo.isEmpty() ->
                    Toast.makeText(this, "Please select whom to raise the concern to", Toast.LENGTH_SHORT).show()

                description.isEmpty() ->
                    Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()

                else -> {
                    confirmSubmitConcern(description)
                }
            }
        }
    }

    private fun confirmSubmitConcern(description: String) {
        AlertDialog.Builder(this)
            .setTitle("Submit concern")
            .setMessage("Are you sure you want to submit?")
            .setCancelable(true)
            .setPositiveButton("Submit") { dialog, _ ->
                dialog.dismiss()
                pendingDescription = description
                Constant.showLoading(this)
                isUploadFilesInServer(Constant.file_)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun isUploadFilesInServer(isFileType: String?) {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles.removeAt(0)
        }
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
                callRaiseConcernApi(pendingDescription)
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
                    AwsUploadedFiles(isFileUrl = fileItem.path, isFileType = fileItem.type.name)
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
                callRaiseConcernApi(pendingDescription)
            } else {
                videoUploading(totalTasks, onTaskComplete)
            }
        } else {
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
                            this@RaiseConcernActivity,
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
                                        callRaiseConcernApi(pendingDescription)
                                    } else if (isAwsUploadingFile.size == isSelectedFileCount) {
                                        videoUploading(totalTasks, onTaskComplete)
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

    private fun videoUploading(totalTasks: Int, onTaskComplete: () -> Unit) {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("player.vimeo.com")) {
                isAwsUploadedFiles.add(
                    AwsUploadedFiles(isFileUrl = fileItem.path, isFileType = fileItem.type.name)
                )
                iterator.remove()
            }
        }

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
            callRaiseConcernApi(pendingDescription)
        }
    }

    override fun onUploadComplete(success: Boolean, iframe: String?, link: String?) {
        runOnUiThread {
            isAwsUploadedFiles.add(
                AwsUploadedFiles(isFileUrl = link.toString(), isFileType = Constant.VIDEO)
            )

            if (isAwsUploadedFiles.size == isTotalSelectedItem) {
                ProgressDialogHelper.dismiss()
                callRaiseConcernApi(pendingDescription)
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
                        this, getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT
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
        } else if (isFileType == Constant.VIDEO) {
            pickVideoLauncher!!.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        } else if (isFileType == Constant.IMAGE) {
            pickImagesLauncher!!.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
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

    private fun callRaiseConcernApi(description: String) {
        val filePathArray = JsonArray()
        for (file in isAwsUploadedFiles) {
            val obj = JsonObject()
            obj.addProperty("url", file.isFileUrl)
            obj.addProperty("type", file.isFileType)
            filePathArray.add(obj)
        }

        val jsonObject = JsonObject().apply {
            addProperty("concern_type_id", selectedConcernTypeId)
            addProperty("raised_to", selectedRaisedTo)
            addProperty("description", description)
            add("file_path", filePathArray)
        }

        appViewModel!!.isRaiseParentConcern(isAccessToken!!, jsonObject, this)
    }

    private fun confirmDeleteConcern(concern: ParentConcern) {
        AlertDialog.Builder(this)
            .setTitle("Delete concern")
            .setMessage("Are you sure you want to delete this concern? This action cannot be undone.")
            .setCancelable(true)
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                deleteConcern(concern.id)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteConcern(concernId: String) {
        Constant.showLoading(this)
        val jsonObject = JsonObject().apply {
            addProperty("id", concernId)
        }
        appViewModel!!.isRemoveConcern(isAccessToken!!, jsonObject, this)
    }

    private fun clearForm() {
        binding.spinnerConcernType.setSelection(0)
        binding.rbManagement.isChecked = false
        binding.rbPrincipal.isChecked = false
        binding.rbClassTeacher.isChecked = false
        binding.edtDescription.text.clear()
        selectedConcernTypeId = ""
        selectedRaisedTo = ""
        pendingDescription = ""

        selectedFiles.clear()
        isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES
        renderDrawableToCacheFile(R.drawable.attachment_with_bg)?.let {
            selectedFiles.add(FileItem(it, FileType.IMAGE))
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun setupConcernListRecycler() {
        concernAdapter = ParentConcernAdapter(concernList, this) { concern ->
            confirmDeleteConcern(concern)
        }
        binding.rvConcernList.layoutManager = LinearLayoutManager(this)
        binding.rvConcernList.adapter = concernAdapter
    }

    private fun callParentConcernListApi() {
        Constant.showLoading(this)
        appViewModel!!.isParentConcernlist(isAccessToken!!, this)
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.rvConcernList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
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
                    this, getString(R.string.only_2_videos_are_allowed), Toast.LENGTH_SHORT
                ).show()
            } else {
                Constant.isFileLimit = 10
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
                this, getString(R.string.could_not_create_file_for_photo), Toast.LENGTH_SHORT
            ).show()
            return
        }

        val photoURI = FileProvider.getUriForFile(
            this, "${applicationContext.packageName}.fileprovider", photoFile
        )

        cameraImageFilePath = photoFile.absolutePath

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera not available on this device", Toast.LENGTH_SHORT).show()
            Log.e("CameraError", "Camera launch failed", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        if (Constant.Remaining == 0) {
            Toast.makeText(
                this,
                "${getString(R.string.Max)} $MAX_FILES ${getString(R.string.files_allowed)}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        fun addPath(uri: Uri) {
            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) {
                Log.d("SkipFile", "Skipping audio/video file: $uri (MIME: $mimeType)")
                return
            }

            val fileName = getFileName(uri)
            val safeMime = mimeType ?: ""

            val type = when {
                safeMime == "application/pdf" || fileName.endsWith(".pdf", true) -> FileType.PDF

                safeMime == "application/msword" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC

                safeMime == "application/vnd.ms-excel" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                        fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL

                safeMime == "application/vnd.ms-powerpoint" ||
                        safeMime == "application/vnd.openxmlformats-officedocument.presentationml.presentation" ||
                        fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT

                safeMime == "text/plain" || fileName.endsWith(".txt", true) -> FileType.TXT

                safeMime.startsWith("image/") ||
                        fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE

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
                            this, getString(R.string.camera_image_file_not_found), Toast.LENGTH_SHORT
                        ).show()
                    }
                } ?: run {
                    Toast.makeText(
                        this, getString(R.string.camera_image_failed), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            PICK_DOCUMENT_REQUEST -> {
                val clipData = data?.clipData
                val singleUri = data?.data

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        addPath(clipData.getItemAt(i).uri)
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
}