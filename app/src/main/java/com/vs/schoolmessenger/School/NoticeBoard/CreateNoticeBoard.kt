package com.vs.schoolmessenger.School.NoticeBoard

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
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeBoardDetails
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
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
    var isNoticeBoardId = ""
    var isNoticeBoardPosition = 0
    private var noticeList: List<NoticeStaffData> = emptyList()
    private var isUpdatingSearchText = false


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

        userDetails = SharedPreference.getUserDetails(this)
//        userDetails?.let {
//            setupSchoolSpinner(it.staff_details)
//        }

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.rytStart.setOnClickListener(this)
//        binding.rytEnd.setOnClickListener(this)
        binding.rytEndDate.setOnClickListener(this)
        binding.txtStartDate.setOnClickListener(this)
        binding.txtEndDate.setOnClickListener(this)
        binding.lnrStartCalendar.setOnClickListener(this)
        binding.lnrEndCalendar.setOnClickListener(this)
//        binding.lnrTabOneName.setOnClickListener(this)
//        binding.lnrTabTwoName.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.btnNext.text = getString(R.string.NEXT)
        val (dayOnly, _, fullDate, _, _) = Constant.getCurrentDateInfo()
        binding.lblDay.text = dayOnly
        binding.lblEndDay.text = dayOnly

        txtStartDate = fullDate
        txtEndDate = fullDate
        val parts = txtStartDate!!.split(" ")
        val Month = parts[1]
        val Year = parts[2]
        binding.txtStartDate.text = Month + " " + Year
        binding.txtEndDate.text = Month + " " + Year

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        saveDrawableToCache(R.drawable.add_image)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }

        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter

//        noticeboardadapter = SchoolNoticeBoardAdapter(
//            emptyList(), this, this, false,
//            binding.nomessage,
//            binding.txtNoData
//        )
//        binding.rcyNoticeBoard.adapter = noticeboardadapter
//        binding.rcyNoticeBoard.layoutManager = LinearLayoutManager(this)
//        binding.rcyNoticeBoard.adapter = noticeboardadapter

//        binding.edtSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (::noticeboardadapter.isInitialized) {
//                    noticeboardadapter.filter.filter(s)
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })



        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)

                    if(Constant.Remaining!! > 0) {
                        Constant.Remaining = Constant.Remaining - selectedUris!!.size
                        selectedUris?.take(Constant.Remaining!!)?.forEach { uri ->
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
                            Log.d("SelectedFile", "URI: $uri, Type: $type")
                        }
                        mAdapter!!.notifyDataSetChanged()

                    }
                }
            }

        appViewModel!!.isEditNoticeBoard?.observe(this) { response ->
            Constant.hideLoading(this@CreateNoticeBoard)
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, this)
            }
        }

//        appViewModel!!.isnoticeboarddelete?.observe(this) { response ->
//            if (response != null) {
//                if (response.status) {
//                    Constant.hideLoading(this@CreateNoticeBoard)
//                    noticeboardadapter!!.removeItemAt(isNoticeBoardPosition)
//                } else {
//                    Constant.showDataValidation(
//                        resources.getString(R.string.fail), response.message, this
//                    )
//                }
//            }
//        }

//
//        binding.txtTitle.filters = arrayOf(InputFilter.LengthFilter(Constant.isTitleLength))
//        binding.txtDesc.filters = arrayOf(InputFilter.LengthFilter(Constant.isDescriptionLength))
//        Constant.editTextCounter(
//            this, binding.txtDesc, Constant.isDescriptionLength, binding.lbTextCount
//        )
//        Constant.editTextCounter(
//            this, binding.txtTitle, Constant.isTitleLength, binding.lbtitleTextCount
//        )

//        appViewModel?.isNoticeBoardStaffReport?.observe(this) { response ->
//            if (response?.status == true && !response.data.isNullOrEmpty()) {
//                binding.rcyNoticeBoard.visibility = View.VISIBLE
//                binding.nomessage.visibility = View.GONE
//                binding.txtNoData.visibility = View.GONE
//                isloadhomeworkData(response.data)
//            } else {
//                isloadhomeworkData(emptyList())
//                binding.rcyNoticeBoard.visibility = View.GONE
//                binding.nomessage.visibility = View.VISIBLE
//                binding.txtNoData.visibility = View.VISIBLE
//            }
//        }
//
//        val channel = NotificationChannel(
//            "reminder_channel", "Reminders", NotificationManager.IMPORTANCE_HIGH
//        )
//        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        manager.createNotificationChannel(channel)

    }

//    private fun loadNoticeData(newData: List<NoticeStaffData>) {
//        Log.d("AdapterUpdate", "New data size: ${newData.size}")
//
//        noticeboardadapter.updateList(newData)
//        binding.rcyNoticeBoard.visibility = View.VISIBLE
//        binding.nomessage.visibility = View.GONE
//        binding.txtNoData.visibility = View.GONE
//    }

//    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {
//        val schoolNames = staffList.map { it.school_name }
//
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schoolNames)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.schoollistfilter.adapter = adapter
//
//        binding.schoollistfilter.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parent: AdapterView<*>, view: View?, position: Int, id: Long
//                ) {
//                    val selectedStaff = staffList[position]
//                    isAccessToken = selectedStaff.access_token
//                    isStaffDetails = selectedStaff
//                    Log.d(
//                        "SpinnerSelection",
//                        "Selected school: ${selectedStaff.school_name}, Token: $isAccessToken"
//                    )
//                    isGetNoticeBoardList()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
//
//        if (staffList.isNotEmpty()) {
//            isAccessToken = staffList[0].access_token
//            isStaffDetails = staffList[0]
//            Log.d("DefaultSelection", "Default token: $isAccessToken")
//        }
//    }

//    private fun isloadhomeworkData(newData: List<NoticeStaffData>?) {
//        Log.d("SearchDebug", "isloadhomeworkData called with ${newData?.size ?: 0} items")
//
//        if (newData != null && newData.isNotEmpty()) {
//            noticeList = newData
//            noticeboardadapter.isLoading = false
//            noticeboardadapter.updateList(newData, true)
//
//            isUpdatingSearchText = true
//            binding.edtSearch.setText("")
//            isUpdatingSearchText = false
//
//            Log.d(
//                "SearchDebug",
//                "Data loaded successfully, adapter item count: ${noticeboardadapter.itemCount}"
//            )
//        } else {
//            noticeList = emptyList()
//            noticeboardadapter.isLoading = false
//            noticeboardadapter.updateList(emptyList(), true)
//
//            isUpdatingSearchText = true
//            binding.edtSearch.setText("")
//            isUpdatingSearchText = false
//
//            Log.d("SearchDebug", "Empty data loaded")
//        }
//    }


//    private fun isGetNoticeBoardList() {
//        binding.rcyNoticeBoard.layoutManager = GridLayoutManager(this, 2)
//        binding.rcyNoticeBoard.isNestedScrollingEnabled = false
//        noticeboardadapter.isLoading = true
//        noticeboardadapter.notifyDataSetChanged()
//
//        appViewModel!!.isNoticeBoardStaffReport(isAccessToken!!, this)
//    }


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
                    Toast.makeText(this, getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT).show()
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
                Constant.selectedFiles.clear()
                Constant.isAwsUploadedFiles.clear()
                onBackPressed()
            }

//            R.id.lnrTabOneName -> {
//
//                binding.txtDesc.setText("")
//                binding.txtTitle.setText("")
//                Constant.selectedFiles.clear()
//                Constant.isAwsUploadedFiles.clear()
//
//                saveDrawableToCache(R.drawable.add_image)?.let {
//                    Constant.selectedFiles.add(
//                        FileItem(
//                            it, FileType.IMAGE
//                        )
//                    )
//                }
//
//                binding.rcyImages.visibility = View.VISIBLE
//                mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
//                binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
//                binding.rcyImages.adapter = mAdapter
//
//
//                binding.btnNext.text = getString(R.string.NEXT)
//                binding.noticeboardCreate.visibility = View.VISIBLE
//                binding.line1.setBackgroundResource(R.color.iconBlue)
//                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
//                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
//                binding.line2.setBackgroundResource(R.color.white)
//                binding.rcyNoticeBoard.visibility = View.GONE
//                binding.rytSearch323.visibility = View.GONE
//                binding.schoollistfilter.visibility = View.GONE
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//            }

//            R.id.lnrTabTwoName -> {
//                binding.btnNext.text = getString(R.string.update_noticeboard)
//                binding.noticeboardCreate.visibility = View.GONE
//                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
//                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
//                binding.line2.setBackgroundResource(R.color.iconBlue)
//                binding.line1.setBackgroundResource(R.color.white)
//                binding.rcyNoticeBoard.visibility = View.VISIBLE
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
//                binding.schoollistfilter.visibility = View.VISIBLE
//                isGetNoticeBoardList()
//            }

//            R.id.imgSearchToolBar -> {
//                if (binding.rytSearch323.isVisible) {
//                    binding.rytSearch323.visibility = View.GONE
//                    binding.edtSearch.setText("")
//                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
//                } else {
//                    binding.rytSearch323.visibility = View.VISIBLE
//                    binding.edtSearch.setText("")
//                    binding.edtSearch.requestFocus()
//                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.showSoftInput(binding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
//                }
//            }

            R.id.txtStartDate, R.id.rytStartDate, R.id.txtStartDate, R.id.lnrStartCalendar -> {
                selectedDateField = 1
                Constant.showDatePicker(this, false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    txtStartDate = Constant.covertDateFormate(selectedDate)
                    val parts = txtStartDate!!.split(" ")
                    val day = parts[0]
                    val Month = parts[1]
                    val Year = parts[2]
                    binding.txtStartDate.text = Month + " " + Year
                    binding.lblDay.text = day
//                    binding.lblDate.text = Date
                }
            }

            R.id.rytEndDate, R.id.lnrEndCalendar, R.id.txtEndDate -> {
                selectedDateField = 2
                Constant.showDatePicker(this, false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    txtEndDate = Constant.covertDateFormate(selectedDate)
                    val parts = txtEndDate!!.split(" ")
                    val day = parts[0]
                    val Month = parts[1]
                    val Year = parts[2]
                    binding.txtEndDate.text = Month + " " + Year
                    binding.lblEndDay.text = day
//                    binding.lblEndDate.text = Date
                }
            }

            R.id.btnNext -> {
                if (binding.btnNext.text.toString() == getString(R.string.update_noticeboard)) {
                    showSendConfirmationDialog(true)
                } else {
                    isRedirectToSchoolList()
                }
            }
        }
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

        if (resultCode != RESULT_OK) return

        if (Constant.Remaining!! == 0) {
            Toast.makeText(this, "${getString(R.string.Max)} ${MAX_FILES} ${getString(R.string.files_allowed)}", Toast.LENGTH_SHORT).show()
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
                        Constant.Remaining = Constant.Remaining - 1

                        addPath(uri)
                    } else {
                        Toast.makeText(this, getString(R.string.camera_image_file_not_found), Toast.LENGTH_SHORT)
                            .show()
                    }
                } ?: run {
                    Toast.makeText(this, getString(R.string.camera_image_failed), Toast.LENGTH_SHORT).show()
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
        return File.createTempFile("${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir)
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

//    fun showEditDeletePopup(data: NoticeStaffData, anchor: View) {
//        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
//        val popupWindow = PopupWindow(
//            popupView,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true
//        )
//        popupWindow.elevation = 10f
//
//        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
//        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)
//
//        layoutEdit.setOnClickListener {
//            isEditProcess(data)
//            popupWindow.dismiss()
//        }
//
//        layoutDelete.setOnClickListener {
//            showSendConfirmationDialog(false)
//            popupWindow.dismiss()
//        }
//        popupWindow.showAsDropDown(anchor, 0, 10)
//    }

    fun showSendConfirmationDialog(isNoticeBoardUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
       // if (isNoticeBoardUpdate) {
            alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_noticeboard)
//        } else {
//            alertMessage.text = getString(R.string.are_you_sure_want_to_delete)
//        }

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
           // if (isNoticeBoardUpdate) {
                ProgressDialogHelper.show(this)
                ProgressDialogHelper.updateProgress(10)
                isUploadFilesInServer(Constant.file_)
//            } else {
//                val jsonObject = JsonObject()
//                jsonObject.addProperty(APIKeyNames.id, isNoticeBoardId)
//                appViewModel?.isnoticeboarddelete(isAccessToken!!, jsonObject, this)
//            }
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    // Edit Update code
    fun isUploadFilesInServer(isFileType: String?) {

        if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_HOMEWORK || SELECTED_SCHOOL_MENU == M_SCHOOL_CLASS_EVENTS || SELECTED_SCHOOL_MENU == M_ASSIGNMENT || SELECTED_SCHOOL_MENU == M_NOTICEBOARD) {
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
                isUpdateNoticeBoard()
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
                                        isUpdateNoticeBoard()
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
            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
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

//    override fun onClickListener(
//        data: NoticeStaffData,
//        anchorView: View,
//        adapterPosition: Int
//    ) {
//        isNoticeBoardId = data.id
//        isNoticeBoardPosition = adapterPosition
//       // showEditDeletePopup(data, anchorView)
//    }

//    override fun onSearchResultEmpty(isEmpty: Boolean) {
//        Log.d("SearchResult", "Search result empty? $isEmpty for query '${binding.edtSearch.text}'")
//        if (isEmpty) {
//            binding.rcyNoticeBoard.visibility = View.GONE
//            binding.nomessage.visibility = View.VISIBLE
//            binding.txtNoData.visibility = View.VISIBLE
//        } else {
//            binding.rcyNoticeBoard.visibility = View.VISIBLE
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//        }
//    }

    fun isEditProcess(data: NoticeStaffData?) {
        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.add_image)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//        binding.line1.setBackgroundResource(R.color.iconBlue)
//        binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
//        binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
//        binding.line2.setBackgroundResource(R.color.white)

        binding.noticeboardCreate.visibility = View.VISIBLE
     //   binding.rcyNoticeBoard.visibility = View.GONE
//        binding.rytSearch323.visibility = View.GONE
    //    binding.schoollistfilter.visibility = View.GONE
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


            binding.txtStartDate.setText(startDate?.let { displayFormat.format(it) } ?: "")
            binding.txtEndDate.setText(endDate?.let { displayFormat.format(it) } ?: "")

        } catch (e: Exception) {
            e.printStackTrace()
            binding.lblDay.text = ""
            binding.lblEndDay.text = ""
            binding.txtStartDate.setText("")
            binding.txtEndDate.setText("")
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
            Constant.selectedFiles.addAll(mappedList)
        }
        if (Constant.selectedFiles.size > 1) {
            binding.rcyImages.visibility = View.VISIBLE
            mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
            binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
            binding.rcyImages.adapter = mAdapter
        } else {
            binding.rcyImages.visibility = View.VISIBLE
            mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
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
        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType
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