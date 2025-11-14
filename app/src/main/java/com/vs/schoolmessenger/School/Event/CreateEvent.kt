package com.vs.schoolmessenger.School.Event

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
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
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.Event.Adapter.EventCategorySpinnerAdapter
import com.vs.schoolmessenger.School.Event.Model.EventCategory
import com.vs.schoolmessenger.School.Event.Model.EventDetails
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
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
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CreateEventBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateEvent : BaseActivity<CreateEventBinding>(), OnImageClickListener,
    View.OnClickListener, OnDateSelectedListener, EventClickListener, TimeSelectedListener,
     VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): CreateEventBinding {
        return CreateEventBinding.inflate(layoutInflater)
    }
    private var isSchoolEventItem: SchoolEventItem? = null
    var isTotalSelectedItem = 0
    private var cameraPermissionDeniedCount = 0
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
        internal const val CAMERA_IMAGE_REQUEST = 1004
    }
    var isSelectedCategory = ""
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isFromTime = true
    private var isStaffDetails: StaffDetails? = null

    private var lastSelectedDate: Calendar? = null


    var selectedDate: Calendar? = null
    private var selectedDateField: Int = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

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
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.btnNext.text = getString(R.string.NEXT)
        binding.txtStartTime.setOnClickListener(this)
        binding.rytHistory.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }

        binding.txtStartTime.text = Constant.getCurrentTime()
        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter

        val (dayOnly, dayOfWeek, fullDate, _) = Constant.getCurrentDateInfo2()
        binding.lblDay.text = dayOfWeek
        binding.txtStartDate.text = fullDate

        lastSelectedDate = Calendar.getInstance()

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
                                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                                fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
                                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
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

                        Toast.makeText(
                            this,
                            "Added $addedCount file${if (addedCount > 1) "s" else ""}",
                            Toast.LENGTH_SHORT
                        ).show()


                        Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")
                    } else if (Constant.Remaining <= 0) {
                        Toast.makeText(this, "You have reached the maximum file limit.", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        appViewModel!!.isEditEvent?.observe(this) { response ->
            Constant.hideLoading(this@CreateEvent)
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isGetEventCategory?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    isLoadCategory(response.data)
                }
            }
        }


        appViewModel?.isGetEventCategories(
            isAccessToken!!, this
        )
    }

    fun isLoadCategory(data: List<EventCategory>) {
        val adapter = EventCategorySpinnerAdapter(this, data)
        binding.isCategorySpinner.adapter = adapter

        binding.isCategorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.d("isSelectedId", data[position].name)
                    isSelectedCategory = data[position].name
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
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
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {
            // Show rationale if user has denied permission before
            if (cameraPermissionDeniedCount >= 2 && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                showCameraPermissionSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
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
                        this,
                        Manifest.permission.CAMERA
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
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.camera_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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

            R.id.rytStartDate -> {

                selectedDateField = 1
                showDatePicker11(this, false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text =
                        Constant.covertDateFormate(selectedDate) // 13 may 2222
                    val (day, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
                    binding.lblDay.text = formattedDate
                }
            }


            R.id.txtStartTime -> {
                isFromTime = true
                showTimePickerDialog1(this, this)

            }

            R.id.rytHistory -> startActivity(Intent(this, EventReport::class.java))


            R.id.btnNext -> {
                if (binding.btnNext.text.toString() == getString(R.string.update_event)) {
                    showSendConfirmationDialog(true)
                } else {
                    RedirectToRecepientActivity()
                }
            }

        }
    }


    override fun onDateSelected(date: String) {
        when (selectedDateField) {
            1 -> binding.txtStartDate.text = date
        }
    }


    override fun onTimeSelected(hour: Int, minute: Int, amPm: String) {
        if (isFromTime) {
            binding.txtStartTime.text =
                String.format(Constant.timeForMateWithAMPM, hour, minute, amPm)
        } else {

        }
    }


    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }



    fun showDatePicker11(
        context: Context,
        dateFormatType: Boolean,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = lastSelectedDate ?: Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Save for next time
                lastSelectedDate = selectedCalendar

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year, month, day
        )

        // Prevent past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }


    fun showTimePickerDialog1(context: Context, listener: TimeSelectedListener) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                // FIXED: Validate AFTER selection (reliable enforcement)
                val today = Calendar.getInstance()
                val effectiveSelectedDate = selectedDate ?: today  // Fallback to today if not set
                val isSameDay = effectiveSelectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        effectiveSelectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                if (isSameDay) {
                    val selectedCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val currentCal = Calendar.getInstance().apply {
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (selectedCal.before(currentCal)) {
                        // Enforce: Reset to current time and notify
                        val resetHour12 = if (currentCal.get(Calendar.HOUR_OF_DAY) == 0) 12
                        else if (currentCal.get(Calendar.HOUR_OF_DAY) > 12) currentCal.get(Calendar.HOUR_OF_DAY) - 12
                        else currentCal.get(Calendar.HOUR_OF_DAY)
                        val resetAmPm = if (currentCal.get(Calendar.HOUR_OF_DAY) < 12) Constant.AM else Constant.PM
                        listener.onTimeSelected(resetHour12, currentCal.get(Calendar.MINUTE), resetAmPm)
                        Toast.makeText(context, "Time cannot be past", Toast.LENGTH_SHORT).show()  // Add this string to strings.xml: "Time cannot be in the past"
                        return@TimePickerDialog
                    }
                }

                // Valid: Proceed with 12-hour format
                val amPm = if (selectedHour < 12) Constant.AM else Constant.PM
                val hourIn12Format = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                listener.onTimeSelected(hourIn12Format, selectedMinute, amPm)
            },
            currentHour,
            currentMinute,
            false  // 12-hour format
        )

        // REMOVED: Hacky OnTimeChangedListener (no longer needed with post-selection validation)

        timePickerDialog.show()
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
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )
                cameraImageFilePath = photoFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
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
                    ".xlsx",
                    true
                ) -> FileType.EXCEL

                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }
            if(Constant.selectedFiles.size < MAX_FILES +1) {
                Constant.selectedFiles.add(FileItem(uri.toString(), type))
            }
            else{
                Constant.Remaining = 0
            }
            for (item in Constant.selectedFiles) {
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


    override fun onClickListener(data: CreateEvent) {
        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }

        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter
    }

    private fun RedirectToRecepientActivity() {

        val txtLocation = binding.txtLocation.text.toString().trim()
        val txtTitle = binding.txtTitle.text.toString().trim()
        val txtDesc = binding.txtDesc.text.toString().trim()
        val txtStartDate = Constant.convertDateFormat(binding.txtStartDate.text.toString())
        val txtStartTime = binding.txtStartTime.text.toString().trim()

        if (txtLocation.isEmpty()) {
            binding.txtLocation.error = getString(R.string.This_field_required)
            binding.txtLocation.requestFocus()
            return
        }

        if (txtTitle.isEmpty()) {
            binding.txtTitle.error = getString(R.string.This_field_required)
            binding.txtTitle.requestFocus()
            return
        }

        if (txtDesc.isEmpty()) {
            binding.txtDesc.error = getString(R.string.This_field_required)
            binding.txtDesc.requestFocus()
            return
        }

        val eventDetails = EventDetails(
            txtLocation,
            txtTitle,
            txtDesc,
            txtStartDate,
            txtStartTime,
            isSelectedCategory
        )

        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.event_data, eventDetails)
        startActivity(intent)
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
        alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_event)


        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
                ProgressDialogHelper.show(this)
                ProgressDialogHelper.updateProgress(10)
                isUploadFilesInServer(Constant.file_)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    // Edit Update code
    fun isUploadFilesInServer(isFileType: String?) {

        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_NOTICEBOARD) {
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
                isUpdateEvent()
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
                                        isUpdateEvent()
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
            isUpdateEvent()
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
                isUpdateEvent()
            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }


    fun isEditProcess(data: SchoolEventItem?) {
        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.attachment_with_bg)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.rytRecyclewview.visibility = View.VISIBLE  // Typo? Assuming rytRecycleView

        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.txtTitle.setText(data!!.title)
        binding.txtDesc.setText(data.description)
        binding.txtLocation.setText(data.venue)
        isSelectedCategory = data.category
        binding.txtStartDate.text = Constant.covertDateFormate(data.date)
        binding.txtStartTime.text = data.time


        try {
            val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())  // Adjust if data.date format differs
            val parsedDate = sdfInput.parse(binding.txtStartDate.text.toString())
            selectedDate = Calendar.getInstance().apply { time = parsedDate!! }
        } catch (e: Exception) {
            Log.e("EditProcess", "Failed to parse date: ${e.message}")
            selectedDate = Calendar.getInstance()  // Fallback to today
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
        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter
    }


    fun isUpdateEvent() {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(this).toString())
        val jsonObject = JsonObject()
        val filePathArray = JsonArray()

        jsonObject.addProperty(APIKeyNames.id, isSchoolEventItem!!.id)
        jsonObject.addProperty(APIKeyNames.title, binding.txtTitle.text.toString())
        jsonObject.addProperty(APIKeyNames.description, binding.txtDesc.text.toString())
        jsonObject.addProperty(APIKeyNames.iframe, "")
        jsonObject.addProperty(APIKeyNames.file_size, "")
        jsonObject.addProperty(APIKeyNames.thumbnail, "")
        jsonObject.addProperty(
            APIKeyNames.event_date,
            Constant.convertDateFormat(binding.txtStartDate.text.toString())
        )
        jsonObject.addProperty(APIKeyNames.event_time, binding.txtStartTime.text.toString().trim())
        jsonObject.addProperty(APIKeyNames.category, isSelectedCategory)
        jsonObject.addProperty(APIKeyNames.venue, binding.txtLocation.text.toString())
        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType
            )
            filePathArray.add(isSelectedObject)
        }
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        appViewModel?.isEventUpdate(isAccessToken!!, jsonObject, this)
    }

    override fun onResume() {
        super.onResume()
        if (Constant.isClickEdit) {
            binding.btnNext.text = getString(R.string.update_event)
            Constant.isClickEdit = false
            isSchoolEventItem = intent.getParcelableExtra<SchoolEventItem>(Constant.event_data)
            isEditProcess(isSchoolEventItem)
        }
    }

}