package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.FCM.NotificationCallScreen
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.ReportBugBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

class ReportTheBug : BaseActivity<ReportBugBinding>(), View.OnClickListener {

    override fun getViewBinding(): ReportBugBinding {
        return ReportBugBinding.inflate(layoutInflater)
    }

    val filePaths = ArrayList<String>()

    private val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 102

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    private val PICK_DOCUMENT_REQUEST = 1003
    private val MAX_FILES = 10

//    private lateinit var menuItems: List<String>
    private var selectedMenu: String? = null
    private var cameraPermissionDeniedCount = 0
    private val CAMERA_IMAGE_REQUEST = 1001
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    override fun setupViews() {
        super.setupViews()
        binding.rlaPickImage.setOnClickListener(this)
        binding.btnReportBug.setOnClickListener(this)
        binding.imgAddNotification.setOnClickListener(this)


        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Report a bug"

//        albumResultLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    val selectedUris =
//                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
//
//                    Log.d("Constant.Remaining", Constant.Remaining.toString())
//
//                    if (Constant.Remaining > 0 && !selectedUris.isNullOrEmpty()) {
//
//                        val previousCount = Constant.selectedFiles.size
//                        Constant.Remaining -= selectedUris.size
//                        selectedUris.forEach { uri ->
//                            val mimeType = contentResolver.getType(uri)
//                            val path = when (uri.scheme) {
//                                Constant.file_ -> uri.path
//                                else -> getPathFromUri(uri)
//                            }
//
//                            if (path == null) {
//                                Log.w("addPath", "Could not resolve path from URI: $uri")
//                                return@forEach
//                            }
//
//                            val fileName = getFileName(uri).ifEmpty { File(path).name }
//
//                            val type = when {
//                                mimeType?.startsWith("image/") == true -> FileType.IMAGE
//                                mimeType?.startsWith("video/") == true -> FileType.VIDEO
//                                mimeType?.startsWith("audio/") == true -> FileType.AUDIO
//                                fileName.endsWith(".pdf", true) -> FileType.PDF
//                                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
//                                fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
//                                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
//                                fileName.endsWith(".txt", true) -> FileType.TXT
//                                else -> FileType.OTHER
//                            }
//
//                            Log.d("MAX_FILES", MAX_FILES.toString())
//
//                            if (Constant.selectedFiles.size < MAX_FILES + 1) {
//                                Constant.selectedFiles.add(FileItem(uri.toString(), type))
//                            } else {
//                                Constant.Remaining = 0
//                            }
//
//                            Log.d("SelectedFile", "URI: $uri, Type: $type")
//                        }
//                        mAdapter?.notifyDataSetChanged()
//                        val addedCount = Constant.selectedFiles.size - previousCount
//                        val totalCount = Constant.selectedFiles.size
//
//                        Toast.makeText(
//                            this,
//                            "Added $addedCount file${if (addedCount > 1) "s" else ""}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//
//                        Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")
//                    } else if (Constant.Remaining <= 0) {
//                        Toast.makeText(this, "You have reached the maximum file limit.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }


        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                    val remaining = MAX_FILES - Constant.selectedFiles.size

                    selectedUris?.take(remaining)?.forEach { uri ->
                        val mimeType = contentResolver.getType(uri)
                        val path = when (uri.scheme) {
                            Constant.file_ -> uri.path
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

                        Constant.selectedFiles.forEach {
                            filePaths.add(it.path)
                        }

                        isLoadTheReportImage(Constant.selectedFiles)
                    }

                    if ((selectedUris?.size ?: 0) > remaining) {
                        Toast.makeText(
                            this,
                            "${getString(R.string.Only)} $remaining ${getString(R.string.files_added_max)}$MAX_FILES)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }


        loadMenu()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaPickImage -> {
                // Check the SDK version and request the appropriate permission
                val readImagePermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES  // Android 13 (API 33) and later
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE  // Pre-Android 13
                    }

                // Check if the permission is granted
                if (ContextCompat.checkSelfPermission(
                        this, readImagePermission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted, proceed to show the bottom dialog or picker
                    showBottomDialog()
                } else {
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(readImagePermission),
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE // Define your constant for the permission code
                    )
                }
            }

            R.id.btnReportBug -> {
                if (selectedMenu != "Select the menu") {
                    sendMailWithAttachment()
                } else {
                    Toast.makeText(this, "Select the menu", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.imgBack -> {
                Constant.selectedFiles.clear()
                onBackPressed()
            }

            R.id.imgAddNotification -> {
                val intent = Intent(this@ReportTheBug, NotificationCallScreen::class.java)
                startActivity(intent)
            }
        }
    }

    private fun sendMailWithAttachment() {
        val email = "support@savyasasy.com"
        val subject = selectedMenu
        val message = binding.edtReportBug.text.toString().trim()

        if (Constant.selectedFiles.isEmpty()) {
            Toast.makeText(this, "Please attach at least one file", Toast.LENGTH_SHORT).show()
            return
        }

        val uris = arrayListOf<Uri>()

        Constant.selectedFiles.forEach { fileItem ->
            try {
                val fileUri = Uri.parse(fileItem.path)

                if ("content".equals(fileUri.scheme, ignoreCase = true)) {
                    // Already content:// URI
                    uris.add(fileUri)
                } else {
                    // Convert raw path -> FileProvider
                    val file = File(fileUri.path ?: return@forEach)
                    val providerUri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.fileprovider",
                        file
                    )
                    uris.add(providerUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.google.android.gm") // force Gmail only
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gmail not installed", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to show the bottom dialog or picker
                    showBottomDialog()
                } else {
                    // Permission denied
                    if (shouldShowRequestPermissionRationale(permissions[0])) {
                        // If the user denied the permission but didn't check "Don't ask again"
                        Toast.makeText(
                            this,
                            "Permission denied. Please allow access to images.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // If the user checked "Don't ask again"
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    // Show a dialog explaining why the permission is needed and guide the user to app settings
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("This app requires permission to access your images. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Constant.selectedFiles.clear()
    }

    override fun onResume() {
       // Constant.selectedFiles.clear()
        super.onResume()
    }

    private fun isLoadTheReportImage(isImageSelected: MutableList<FileItem>) {
        binding.imgPreview.numColumns = 2
        binding.imgPreview.verticalSpacing = 8
        binding.imgPreview.horizontalSpacing = 8 // optional, spacing between columns
        var courseAdapter: ImagePreviewAdapter? = null

        courseAdapter = ImagePreviewAdapter(
            isImageSelected,
            this,
            object : ImagePreviewRemoveListener {
                override fun add(isAddingId: Int?) {
                    // not used
                }

                override fun remove(isRemovingId: Int) {
                    if (isRemovingId >= 0 && isRemovingId < isImageSelected.size) {
                        isImageSelected.removeAt(isRemovingId)
                        if (isRemovingId < Constant.selectedFiles.size) {
                            Constant.selectedFiles.removeAt(isRemovingId)
                        }

                        // Refresh adapter and height
                        binding.imgPreview.adapter = this@ReportTheBug.let { courseAdapter }
                        Constant.setGridViewHeight(binding.imgPreview, 2)

                    }
                }
            }
        )
        binding.imgPreview.adapter = courseAdapter
        Constant.setGridViewHeight(binding.imgPreview, 2)
    }


    private fun openAlbumSelectActivity(isFileType: String) {

        Log.d("FileComing", isFileType)
        val sdkInt = Build.VERSION.SDK_INT
        if (isFileType == Constant.DOCUMENT && sdkInt < Build.VERSION_CODES.R) {
            openSystemDocumentPicker()
        } else {

            val intent = Intent(this, AlbumSelectActivity::class.java)
            intent.putExtra(Constant.isFileType, isFileType)
            intent.putExtra("ReportBugMenu", true)
            albumResultLauncher.launch(intent)
        }
    }

    // Opens the system file picker for DOCUMENT on Android 10 and below
    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST)
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
                Toast.makeText(
                    this,
                    getString(R.string.only_2_videos_are_allowed),
                    Toast.LENGTH_SHORT
                ).show()
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
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.could_not_create_file_for_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val remaining = MAX_FILES - Constant.selectedFiles.size
        if (remaining <= 0) {
            Toast.makeText(
                this,
                "${getString(R.string.Max)} ${MAX_FILES} ${getString(R.string.files_allowed)}",
                Toast.LENGTH_SHORT
            ).show()
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
            Constant.selectedFiles.forEach {
                filePaths.add(it.path)
            }

            isLoadTheReportImage(Constant.selectedFiles)
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
                        addPath(uri)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.camera_image_file_not_found),
                            Toast.LENGTH_SHORT
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
                } else if (singleUri != null) {
                    addPath(singleUri)
                }
            }
        }
        mAdapter?.notifyDataSetChanged()
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
        return File.createTempFile(
            "${Constant.IMG_}${timeStamp}${Constant.underscore}",
            ".jpg",
            storageDir
        )
    }



    private fun loadMenu() {
//        menuItems = listOf(
//            "Select the menu",
//            "Communication",
//            "Assignment",
//            "Attachments",
//            "Homework",
//            "Student Attendance Marking",
//            "Punch Attendance",
//            "Fee Details",
//            "Events",
//            "Notice Board",
//            "PTM",
//            "Lesson Plan",
//            "LSRW",
//            "QUIZ",
//            "Student Attendance Report",
//            "Staff Attendance Report",
//            "Messages from management",
//            "Student Report",
//            "Daily Collection",
//            "Fee Pending Report"
//        )
        Log.d("DropdownMenuList",Constant.menuNameList.toString())

        val adapter = SpinnerLoadingAdapter(this,Constant.menuNameList)
        binding.isMenuSpinner.adapter = adapter

        binding.isMenuSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                selectedMenu = Constant.menuNameList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}