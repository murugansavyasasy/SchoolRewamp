package com.vs.schoolmessenger.School.NoticeBoard

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
import android.os.Bundle
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
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeBoardDetails
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.isCommunicationType
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CreateNoticeBoardBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateNoticeBoard : BaseActivity<CreateNoticeBoardBinding>(), OnImageClickListener,
    OnDateSelectedListener, View.OnClickListener,
    VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): CreateNoticeBoardBinding {
        return CreateNoticeBoardBinding.inflate(layoutInflater)
    }

    private var noticeboardData: NoticeStaffData? = null
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var selectedDateField: Int = 0
    private var txtStartDate: String? = null
    private var txtEndDate: String? = null
    lateinit var noticeboardadapter: SchoolNoticeBoardAdapter
    private var userDetails: UserDetails? = null
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isTotalSelectedItem = 0
    private val CAMERA_IMAGE_REQUEST = 1001


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        Constant.Remaining = MAX_FILES

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        userDetails = SharedPreference.getUserDetails(this)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.rytHistory.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.rytStart.setOnClickListener(this)
        binding.rytEndDate.setOnClickListener(this)
        binding.txtStartDate.setOnClickListener(this)
        binding.txtEndDate.setOnClickListener(this)
        binding.lnrStartCalendar.setOnClickListener(this)
        binding.lnrEndCalendar.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.btnNext.text = getString(R.string.NEXT)
        val (_, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()

        binding.lblDay.text = dayOfWeek
        binding.lblEndDay.text = dayOfWeek

        txtStartDate = fullDate
        txtEndDate = fullDate

        binding.txtStartDate.text = txtStartDate
        binding.txtEndDate.text = txtEndDate


        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }

        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter


        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)

                    Log.d("Constant.Remaining", Constant.Remaining.toString())

                    if (Constant.Remaining > 0 && !selectedUris.isNullOrEmpty()) {

                        val previousCount = selectedFiles.size
                        Constant.Remaining -= selectedUris.size
                        selectedUris.forEach { uri ->
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
                                    ".docx",
                                    true
                                ) -> FileType.DOC

                                fileName.endsWith(".xls", true) || fileName.endsWith(
                                    ".xlsx",
                                    true
                                ) -> FileType.EXCEL

                                fileName.endsWith(".ppt", true) || fileName.endsWith(
                                    ".pptx",
                                    true
                                ) -> FileType.PPT

                                fileName.endsWith(".txt", true) -> FileType.TXT
                                else -> FileType.OTHER
                            }

                            Log.d("MAX_FILES", MAX_FILES.toString())

                            if (selectedFiles.size < MAX_FILES + 1) {
                                selectedFiles.add(FileItem(uri.toString(), type))
                            } else {
                                Constant.Remaining = 0
                            }

                            Log.d("SelectedFile", "URI: $uri, Type: $type")
                        }
                        mAdapter?.notifyDataSetChanged()
                        val addedCount = selectedFiles.size - previousCount
                        val totalCount = selectedFiles.size

//                        Toast.makeText(
//                            this,
//                            "${getString(R.string.Added)} $addedCount ${getString(R.string.file)}${
//                                if (addedCount > 1) "${
//                                    getString(
//                                        R.string.s_
//                                    )
//                                }" else ""
//                            }",
//                            Toast.LENGTH_SHORT
//                        ).show()


                        Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")
                    } else if (Constant.Remaining <= 0) {
                        //  Toast.makeText(this, getString(R.string.you_have_reached_the_maximum_file_limit), Toast.LENGTH_SHORT).show()
                    }
                }
            }



        appViewModel!!.isEditNoticeBoard?.observe(this) { response ->
            Constant.hideLoading(this@CreateNoticeBoard)
            if (response != null) {
                Log.d("Response", response.status.toString())
                showTopAlertPopup(response.message, this)
            }
        }

        initializeDefaultDates()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("cameraImageFilePath", cameraImageFilePath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        cameraImageFilePath = savedInstanceState.getString("cameraImageFilePath")
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
                        getString(R.string.camera_permission_is_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
            val intent = Intent(activity, NoticeBoardReport::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
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
        val sdkInt = Build.VERSION.SDK_INT
        if (isFileType == Constant.DOCUMENT) {
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
        selectedFiles.clear()
        isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                selectedFiles.clear()
                isAwsUploadedFiles.clear()
                onBackPressed()
            }


            R.id.txtStartDate, R.id.rytStartDate, R.id.lnrStartCalendar -> {
                selectedDateField = 1

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val defaultCal = Calendar.getInstance()

                // Use previously selected date if available
                if (!txtStartDate.isNullOrEmpty()) {
                    try {
                        defaultCal.time = sdf.parse(txtStartDate!!) ?: Date()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Constant.DatePicker(this, false, defaultDate = defaultCal) { selectedDate ->
                    txtStartDate = Constant.covertDateFormate(selectedDate)
                    val (_, formattedDate) = Constant.getDayAndDateOnly2(txtStartDate.toString())// 13 Monday
                    binding.lblDay.text = formattedDate
                    binding.txtStartDate.text = txtStartDate

                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val startDate = sdf.parse(txtStartDate!!) ?: Date()
                    val cal = Calendar.getInstance()
                    cal.time = startDate
                    cal.add(Calendar.DAY_OF_MONTH, 30)
                    val endDate = cal.time
                    txtEndDate = sdf.format(endDate)
                    val (_, endformattedDate) = Constant.getDayAndDateOnly2(txtEndDate.toString())// 13 Monday

                    binding.lblEndDay.text = endformattedDate
                    binding.txtEndDate.text = txtEndDate

                }
            }

            R.id.rytEndDate, R.id.lnrEndCalendar, R.id.txtEndDate -> {
                selectedDateField = 2

                if (txtStartDate.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_a_start_date_first), Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val startDate = sdf.parse(txtStartDate!!) ?: Date()

                val cal = Calendar.getInstance()
                cal.time = startDate
                val minDate = cal.timeInMillis
                val maxDate = Long.MAX_VALUE

                val defaultEndDate = try {
                    sdf.parse(txtEndDate ?: "") ?: Date()
                } catch (e: Exception) {
                    Date()
                }
                val defaultCal = Calendar.getInstance()
                defaultCal.time = defaultEndDate

                Constant.DatePicker(
                    context = this,
                    dateFormatType = false,
                    defaultDate = defaultCal,
                    minDate = minDate,
                    maxDate = maxDate
                ) { selectedDate ->
                    txtEndDate = Constant.covertDateFormate(selectedDate)
                    val (_, endformattedDate) = Constant.getDayAndDateOnly2(txtEndDate.toString())
                    binding.lblEndDay.text = endformattedDate
                    binding.txtEndDate.text = txtEndDate
                }
            }


            R.id.btnNext -> {
                if (binding.btnNext.text.toString() == getString(R.string.update_noticeboard)) {
                    showSendConfirmationDialog(true)
                } else {
                    isRedirectToSchoolList()
                }
            }

            R.id.rytHistory -> startActivity(Intent(this, NoticeBoardReport::class.java))
        }
    }

    private fun initializeDefaultDates() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val today = Calendar.getInstance().time
        txtStartDate = sdf.format(today)
        val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
        binding.lblDay.text = formattedDate
        binding.txtStartDate.text = txtStartDate
        val cal = Calendar.getInstance()
        cal.time = today
        cal.add(Calendar.DAY_OF_MONTH, 30)
        val endDate = cal.time

        txtEndDate = sdf.format(endDate)
        val (_, endformattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday

        binding.lblEndDay.text = endformattedDate
        binding.txtEndDate.text = txtEndDate

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
                    getString(R.string.only_2_videos_are_allowed),
                    Toast.LENGTH_SHORT
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
                    Constant.Remaining = Constant.Remaining - clipData.itemCount

                } else if (singleUri != null) {
                    addPath(singleUri)
                    Constant.Remaining = Constant.Remaining - 1

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

    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.txtStartDate.text = date
            2 -> binding.txtEndDate.text = date
        }
    }

    private fun isRedirectToSchoolList() {
        val title = binding.txtTitle.text.toString().trim()
        val description = binding.txtDesc.text.toString().trim()
        val txtEndDate = Constant.convertDateFormat(txtEndDate!!)
        val txtStartDate = Constant.convertDateFormat(txtStartDate!!)
        if (title.isEmpty()) {
            binding.txtTitle.error = getString(R.string.This_field_required)
            binding.txtTitle.requestFocus()
            return
        }

        if (description.isEmpty()) {
            binding.txtDesc.error = getString(R.string.This_field_required)
            binding.txtDesc.requestFocus()
            return
        }

        val noticeboardDetails = NoticeBoardDetails(title, description, txtStartDate, txtEndDate)

        val intent = Intent(this, SchoolList::class.java)
        intent.putExtra(Constant.notice_data, noticeboardDetails)
        startActivity(intent)
    }


    fun showSendConfirmationDialog(isNoticeBoardUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_noticeboard)
        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            ProgressDialogHelper.show(this)
//            ProgressDialogHelper.updateProgress(10)
            isUploadFilesInServer(Constant.file_)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    // Edit Update code
    fun isUploadFilesInServer(isFileType: String?) {

        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_NOTICEBOARD) {
            selectedFiles.removeAt(0) // Remove '+' placeholder
        }
//        ProgressDialogHelper.updateProgress(50)
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
        }
//        ProgressDialogHelper.updateProgress(80)
    }


    private fun isFileUploadInAws(
        isFileType: String?,
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        isAwsUploadedFiles.clear()
        val iterator = selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
                isUpdateNoticeBoard()
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
                            isStaffDetails!!.school_id,
                            isFileType!!,
                            this,
                            isCountryId!!,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?, isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = selectedFiles[i].type.name
                                        )
                                    )
                                    onTaskComplete()

                                    if (isTotalSelectedItem == isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
                                        isUpdateNoticeBoard()
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
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
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
            isUpdateNoticeBoard()
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
                isUpdateNoticeBoard()
            }
        }
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }


    fun isEditProcess(data: NoticeStaffData?) {
        isAwsUploadedFiles.clear()
        selectedFiles.clear()
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.noticeboardCreate.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.txtTitle.setText(data!!.title)
        binding.txtDesc.setText(data.description)


        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEE, MMM yyyy", Locale.getDefault())

        try {
            val startDate = data.visible_from?.let { inputFormat.parse(it) }
            val endDate = data.visible_to?.let { inputFormat.parse(it) }


            binding.lblDay.text = startDate?.let { dayFormat.format(it) } ?: ""
            binding.lblEndDay.text = endDate?.let { dayFormat.format(it) } ?: ""


            binding.txtStartDate.text = startDate?.let { displayFormat.format(it) } ?: ""
            binding.txtEndDate.text = endDate?.let { displayFormat.format(it) } ?: ""

        } catch (e: Exception) {
            e.printStackTrace()
            binding.lblDay.text = ""
            binding.lblEndDay.text = ""
            binding.txtStartDate.text = ""
            binding.txtEndDate.text = ""
        }



        if (data.file_path.isNotEmpty()) {
            val mappedList = data.file_path.map { filePath ->
                val fileType = try {
                    FileType.valueOf(filePath.type.uppercase())
                } catch (e: IllegalArgumentException) {
                    FileType.OTHER
                }
                FileItem(path = filePath.url, type = fileType)
            }
            selectedFiles.addAll(mappedList)
        }
        if (selectedFiles.size > 1) {
            binding.rcyImages.visibility = View.VISIBLE
            mAdapter = ImagePickingAdapter(this, selectedFiles, this)
            binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
            binding.rcyImages.adapter = mAdapter
        } else {
            binding.rcyImages.visibility = View.VISIBLE
            mAdapter = ImagePickingAdapter(this, selectedFiles, this)
            binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
            binding.rcyImages.adapter = mAdapter
        }
    }

    fun isUpdateNoticeBoard() {

        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(this).toString())
        val jsonObject = JsonObject()
        val filePathArray = JsonArray()
        val txtEndDate = Constant.convertDateFormat(txtEndDate!!)
        val txtStartDate = Constant.convertDateFormat(txtStartDate!!)
        jsonObject.addProperty(APIKeyNames.id, noticeboardData!!.id)
        jsonObject.addProperty(APIKeyNames.title, binding.txtTitle.text.toString())
        jsonObject.addProperty(APIKeyNames.description, binding.txtDesc.text.toString())
        jsonObject.addProperty(APIKeyNames.iframe, "")
        jsonObject.addProperty(APIKeyNames.visible_from, txtStartDate)
        jsonObject.addProperty(APIKeyNames.visible_to, txtEndDate)
        jsonObject.addProperty(APIKeyNames.file_size, "")
        jsonObject.addProperty(APIKeyNames.thumbnail, "")
        for (i in isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type, isAwsUploadedFiles[i].isFileType
            )
            filePathArray.add(isSelectedObject)
        }
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        appViewModel?.isNoticeBoardUpdate(isAccessToken!!, jsonObject, this)

    }

    override fun onResume() {
        super.onResume()
        if (Constant.isClickEdit) {
            binding.btnNext.text = getString(R.string.update_noticeboard)
            Constant.isClickEdit = false
            noticeboardData = intent.getParcelableExtra<NoticeStaffData>(Constant.notice_data)
            isEditProcess(noticeboardData)
        }
    }
}