package com.vs.schoolmessenger.School.Assignment

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
import android.os.Bundle
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
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Parent.Assignment.AssignmentAdapter
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentSendingData
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.AssignmentBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AssignmentCreate : BaseActivity<AssignmentBinding>(), AssignmentClickListener,
    View.OnClickListener, AssignmentStudentListClickListener, OnImageClickListener,
    TimeSelectedListener, OnDateSelectedListener, VimeoVideoUpload.UploadCompletionListener {

    private lateinit var adapter: AssignmentStudentListAdapter

    private var assignmentData: AssignmentData? = null

    override fun getViewBinding(): AssignmentBinding {
        return AssignmentBinding.inflate(layoutInflater)
    }


    var isAssignmentId = ""
    var isAssignmentPosition = 0
    var isTotalSelectedItem = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    private val itemsCategory = listOf(
        "General", "Class Work", "Research Paper", "Project"
    )

    var isAssignmentType = ""
    var isSelectedDate = ""
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0


    private var originalTitle: String? = null
    private var originalDescription: String? = null

    private var originalCategory: String? = null
    private var originalDate: String? = null
    private var originalTime: String? = null
    private var originalFiles: List<FileItem> = emptyList()

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isAcademicYear: List<AcademicYear>? = null
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        Constant.Remaining = MAX_FILES


        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.lnrTabOneName.setOnClickListener(this)
        binding.lnrTabTwoName.setOnClickListener(this)
        binding.rytHistory.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        binding.lblTimePick.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        binding.btnChooseRecipient.text = getString(R.string.ChooseRecipients)
        binding.rcyImages.visibility = View.VISIBLE

        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter



        isSelectedDate = Constant.getCurrentDate()

        binding.txtStartDate.text = Constant.convertToReadableDate(isSelectedDate)
        val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
        binding.lblDay.text = formattedDate
        Log.d("formattedDate", formattedDate)
        Log.d("formattedDate", binding.lblDay.text.toString())
        binding.lblTimePick.text = Constant.getCurrentTime()

        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)

                    Log.d("Constant.Remaining", Constant.Remaining.toString())

                    if (Constant.Remaining > 0 && !selectedUris.isNullOrEmpty()) {

                        val previousCount = Constant.selectedFiles.size
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

        appViewModel!!.isAssignmentUpdate?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this@AssignmentCreate)
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        spinnerType()
    }


    private fun spinnerType() {

        val adapter = SpinnerLoadingAdapter(this, itemsCategory)
        binding.spinnerType.adapter = adapter

        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                isAssignmentType = itemsCategory[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnChooseRecipient -> {
                if (binding.btnChooseRecipient.text.toString() == getString(R.string.update_assignment)) {
                    if (!hasChanges()) {
                        Toast.makeText(
                            this,
                            getString(R.string.no_changes_detected), Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showSendConfirmationDialog(true)
                    }
                } else {
                    isRedirectToSectionStudents()
                }
            }


            R.id.lblTimePick -> {
                showTimePickerDialog(this, this)
            }

            R.id.rytStartDate -> {
                AssignmentCustomshowDatePickerDialog(this, this, isSelectedDate)
            }

            R.id.rytHistory -> startActivity(Intent(this, AssignmentReport::class.java))


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
                        getString(R.string.camera_permission_is_required),
                        Toast.LENGTH_SHORT
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
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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

    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        binding.lblTimePick.text = String.format(Constant.timeForMateWithAMPM, hour, minute, amPm)
    }

    override fun onDateSelected(date: String) {
        isSelectedDate = date
        binding.txtStartDate.text = Constant.convertToReadableDate(date)
        val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
        binding.lblDay.text = formattedDate
        Log.d("isSelectedDate", date)
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
                startActivityForResult(intent, CreateEvent.CAMERA_IMAGE_REQUEST)
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
            if (Constant.selectedFiles.size < MAX_FILES + 1) {
                Constant.selectedFiles.add(FileItem(uri.toString(), type))
            } else {
                Constant.Remaining = 0
            }
            for (item in Constant.selectedFiles) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
            }
        }

        when (requestCode) {
            CreateEvent.CAMERA_IMAGE_REQUEST -> {
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
                        ).show()
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

    private fun isRedirectToSectionStudents() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        if (title.isEmpty()) {
            binding.edtTitle.error = getString(R.string.This_field_required)
            binding.edtTitle.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.edtDescription.error = getString(R.string.This_field_required)
            binding.edtDescription.requestFocus()
            return
        }

        val isAssignmentSendingData = AssignmentSendingData(
            title,
            description,
            isAssignmentType,
            isSelectedDate,
            ""
        )
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.assignment_data, isAssignmentSendingData)
        startActivity(intent)
    }

    override fun onSubmittedClick(data: AssignmentData) {
        val intent = Intent(this, AssignmentStudentList::class.java)
        intent.putExtra(Constant.assignment_id, data.id)
        intent.putExtra(Constant.type, Constant.SUBMITTED)
        startActivity(intent)
    }

    override fun onEditAndDeleteClick(
        data: AssignmentData, anchorView: View, adapterPosition: Int
    ) {
        isAssignmentId = data.id
        isAssignmentPosition = adapterPosition
        showEditDeletePopup(data, anchorView)
    }

    override fun onNotSubmittedClick(data: AssignmentData) {
        val intent = Intent(this, AssignmentStudentList::class.java)
        intent.putExtra(Constant.assignment_id, data.id)
        intent.putExtra(Constant.type, Constant.NOTSUBMITTED)
        startActivity(intent)
    }

    override fun onItemClick(
        data: AssignmentData,
        holder: AssignmentAdapter.DataViewHolder
    ) {
        TODO("Not yet implemented")
    }

    override fun onReadStatusClick(
        isData: ParentAssignmentData,
        isPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onClickListener(
        data: SubmittedAssignment,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }


    fun showEditDeletePopup(data: AssignmentData, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        layoutEdit.setOnClickListener {
            isEditProcess(data)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }

    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_assignment)
        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            // if (isEventUpdate) {
            ProgressDialogHelper.show(this)
//            ProgressDialogHelper.updateProgress(10)
            isUploadFilesInServer(Constant.file_)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    // Edit Update code
    fun isUploadFilesInServer(isFileType: String?) {

        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_NOTICEBOARD) {
            Constant.selectedFiles.removeAt(0) // Remove '+' placeholder
        }
//        ProgressDialogHelper.updateProgress(50)
        ProgressDialogHelper.updateProgress(0)
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


        val numNonVideoFiles = Constant.selectedFiles.size
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
            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(
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
                isAssignmentUpdate()
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
                    onTaskComplete()
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
                                    onTaskComplete()

                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
                                        isAssignmentUpdate()
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading(totalTasks, onTaskComplete)
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    Log.d("isUploadIssue", error.toString())
                                    onTaskComplete()
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
            isAssignmentUpdate()
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
                isAssignmentUpdate()
            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }


    fun isEditProcess(data: AssignmentData?) {
        if (data == null) return


        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(FileItem(it, FileType.IMAGE))
        }


        originalTitle = data.title
        originalDescription = data.description
        originalCategory = data.category
        originalDate = Constant.covertDateFormate(data.created_date)
        originalTime = data.created_time

        if (data.file_path.isNotEmpty()) {
            val mappedList = data.file_path.map { filePath ->
                val fileType = try {
                    FileType.valueOf(filePath.type.uppercase())
                } catch (e: IllegalArgumentException) {
                    FileType.OTHER
                }
                FileItem(path = filePath.url, type = fileType)
            }
            Constant.selectedFiles.addAll(mappedList)
            originalFiles = mappedList
        }


        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.rytRecyclewview.visibility = View.VISIBLE
        binding.line1.setBackgroundResource(R.color.iconBlue)
        binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
        binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.line3.setBackgroundResource(R.color.white)

        binding.edtTitle.setText(data.title)
        binding.edtDescription.setText(data.description)


        val adapter = binding.spinnerType.adapter
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString().equals(data.category, ignoreCase = true)) {
                    binding.spinnerType.setSelection(i)
                    break
                }
            }
        }

        binding.txtStartDate.text = Constant.covertDateFormate(data.created_date)
        val (_, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
        binding.lblDay.text = formattedDate
        binding.lblTimePick.text = data.created_time


        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter
    }

    private fun hasChanges(): Boolean {
        val currentTitle = binding.edtTitle.text.toString().trim()
        val currentDescription = binding.edtDescription.text.toString().trim()
        val currentCategory = binding.spinnerType.selectedItem?.toString()
        val currentDate = binding.txtStartDate.text.toString().trim()
        val currentTime = binding.lblTimePick.text.toString().trim()
        val currentFiles = Constant.selectedFiles.filter { it.type != FileType.IMAGE }

        return currentTitle != originalTitle ||
                currentDescription != originalDescription ||
                currentCategory != originalCategory ||
                currentDate != originalDate ||
                currentTime != originalTime ||
                currentFiles != originalFiles
    }


    override fun onResume() {
        super.onResume()
        if (Constant.isClickEdit) {
            binding.btnChooseRecipient.text = getString(R.string.update_assignment)
            Constant.isClickEdit = false
            assignmentData = intent.getParcelableExtra<AssignmentData>(Constant.assignment_data)
            isEditProcess(assignmentData)
        }
    }

    fun isAssignmentUpdate() {
        val jsonObject = JsonObject()
        val filePathArray = JsonArray()

        jsonObject.addProperty(APIKeyNames.id, assignmentData!!.id)
        jsonObject.addProperty(APIKeyNames.title, binding.edtTitle.text.toString())
        jsonObject.addProperty(APIKeyNames.description, binding.edtDescription.text.toString())
        jsonObject.addProperty("category", isAssignmentType)
        jsonObject.addProperty("submission_date", isSelectedDate)
        jsonObject.addProperty(APIKeyNames.iframe, assignmentData!!.iframe)
        jsonObject.addProperty(APIKeyNames.file_size, assignmentData!!.file_size)
        jsonObject.addProperty(APIKeyNames.thumbnail, "")
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type,
                Constant.isAwsUploadedFiles[i].isFileType.toString()
            )
            filePathArray.add(isSelectedObject)
        }
        appViewModel!!.assignmentUpdate(isAccessToken!!, jsonObject, this)
    }


    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES
        super.onBackPressed()
    }

}