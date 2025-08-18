package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
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
    OnImageClickListener,VimeoVideoUpload.UploadCompletionListener  {

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
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.btnChooseRecipient.setOnClickListener(this)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails!!.access_token
        assignmentId = intent.getStringExtra("assignment_id")
        titleName = intent.getStringExtra("title")
        subjectName = intent.getStringExtra("subject")

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Submit your assignment"
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text = isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        binding.edtTitle.setText(titleName)
        saveDrawableToCache(R.drawable.add_image)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }

        appViewModel!!.isSubmitAssignment?.observe(this) { response ->
            Constant.hideLoading(this@MyAssignmentSubmit)
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, this)

            }
        }

        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter


        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                    val remaining = MAX_FILES - Constant.selectedFiles.size

                    selectedUris?.take(remaining)?.forEach { uri ->
                        val mimeType = contentResolver.getType(uri)
                        val path = when (uri.scheme) {
                            "file" -> uri.path
                            else -> getPathFromUri(uri)
                        }

                        if (path == null) {
                            Log.w("addPath", "Could not resolve path from URI: $uri")
                            return@forEach
                        }

                        val fileName = getFileName(uri).ifEmpty { File(path).name }
                        val type = when {
                            mimeType?.startsWith("image/") == true -> FileType.IMAGE
                            mimeType?.startsWith("video/") == true -> FileType.VIDEO
                            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                            fileName.endsWith(".pdf", true) -> FileType.PDF
                            fileName.endsWith(".doc", true) || fileName.endsWith(
                                ".docx", true
                            ) -> FileType.DOC

                            fileName.endsWith(".xls", true) || fileName.endsWith(
                                ".xlsx", true
                            ) -> FileType.EXCEL

                            fileName.endsWith(".ppt", true) || fileName.endsWith(
                                ".pptx", true
                            ) -> FileType.PPT

                            fileName.endsWith(".txt", true) -> FileType.TXT
                            else -> FileType.OTHER
                        }

                        Constant.selectedFiles.add(FileItem(uri.toString(), type))

                        Log.d("SelectedFile", "URI: $uri, Type: $type")
                    }

                    if ((selectedUris?.size ?: 0) > remaining) {
                        Toast.makeText(
                            this,
                            "Only $remaining files added (max ${MAX_FILES})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

    private fun getPathFromUri(uri: Uri): String? {
        // Content scheme
        if (uri.scheme.equals("content", ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(columnIndex)
                }
            }
        }

        // File scheme fallback
        if (uri.scheme.equals("file", ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
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
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnChooseRecipient -> {
                showSendConfirmationDialog()
            }
        }
    }


    fun showSendConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = "Are you sure want to submit?"

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            ProgressDialogHelper.show(this)
            ProgressDialogHelper.updateProgress(10)
            isUploadFilesInServer("file")

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    fun isUploadFilesInServer(isFileType: String?) {
        Constant.selectedFiles.removeAt(0)
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
                isAssignmentSend()
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
                            isChildDetails!!.school_id,
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
                                        isAssignmentSend()
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
            isAssignmentSend()
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
                isAssignmentSend()
            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
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
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Camera permission is permanently denied. Please enable it from app settings.")
            .setCancelable(false).setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
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
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST)
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
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
            val selectedVideoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(this, "Only 2 videos are allowed", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Could not create file for photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val remaining = MAX_FILES - Constant.selectedFiles.size
        if (remaining <= 0) {
            Toast.makeText(this, "Max ${MAX_FILES} files allowed", Toast.LENGTH_SHORT).show()
            return
        }

        fun addPath(uri: Uri) {
            Log.d("isFilePickingUrl", uri.toString())
            if (Constant.selectedFiles.size >= MAX_FILES) return

            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) {
                Log.d("SkipFile", "Skipping audio/video file: $uri (MIME: $mimeType)")
                return
            }

            val fileName = getFileName(uri)
            val type = when {
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                fileName.endsWith(".xls", true) || fileName.endsWith(
                    ".xlsx", true
                ) -> FileType.EXCEL

                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            Constant.selectedFiles.add(FileItem(uri.toString(), type))
            for (item in Constant.selectedFiles) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
            }
        }

        when (requestCode) {
            CreateEvent.Companion.CAMERA_IMAGE_REQUEST -> {
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
                        addPath(uri)
                    } else {
                        Toast.makeText(this, "Camera image file not found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } ?: run {
                    Toast.makeText(this, "Camera image failed", Toast.LENGTH_SHORT).show()
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
                } else if (singleUri != null) {
                    addPath(singleUri)
                }
            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    fun isAssignmentSend() {

        val jsonObject = ApiCallRequest.isSubmitAssignment(
            id = assignmentId!!,
            description = binding.edtDescription.text.toString(),
            iframe = "",
            file_size = ""
        )
        appViewModel!!.isSubmitAssignment(isAccessToken!!, jsonObject, this)
    }
}