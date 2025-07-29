package com.vs.schoolmessenger.School.Event

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
import android.text.InputFilter
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventCompletedAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventUpcomingAdapter
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.EventDetails
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CreateEventBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEvent : BaseActivity<CreateEventBinding>(), OnImageClickListener,
    View.OnClickListener, OnDateSelectedListener, EventClickListener, TimeSelectedListener,
    SchoolEventClickListener {

    override fun getViewBinding(): CreateEventBinding {
        return CreateEventBinding.inflate(layoutInflater)
    }

    private var cameraPermissionDeniedCount = 0

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        private const val MAX_FILES = 10

        internal const val CAMERA_IMAGE_REQUEST = 1004
    }


    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    var isFromTime = true
    private var isStaffDetails: StaffDetails? = null
    private var selectedDateField: Int = 0

    lateinit var schooleventAdapter: SchoolEventAdapter

    lateinit var eventupcomingadapter: SchoolEventUpcomingAdapter
    lateinit var eventcompletedadapter: SchoolEventCompletedAdapter

    private var allOngoingEvents: List<SchoolEventItem>? = null
    private var allUpcomingEvents: List<SchoolEventItem>? = null
    private var allCompletedEvents: List<SchoolEventItem>? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.txtStartTime.setOnClickListener(this)
        binding.lnrTabOneName.setOnClickListener(this)
        binding.lnrTabTwoName.setOnClickListener(this)
        Constant.editTextCounter(this, binding.txtDesc, 500, binding.lbTextCount)
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

        binding.txtStartTime.text = Constant.getCurrentTime()
        binding.rcyImages.visibility = View.VISIBLE
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter

        val (dayOnly, dayOfWeek, fullDate, slashDate) = Constant.getCurrentDateInfo()
        binding.lblDate.text = dayOnly
        binding.lblDay.text = dayOfWeek

        binding.txtStartDate.text = fullDate
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

                        Constant.selectedFiles.add(FileItem(uri.toString(), type))

                        if (type.toString() == Constant.VIDEO) {
                            binding.thumbnailView.visibility = View.VISIBLE
                            binding.rcyImages.visibility = View.GONE

                            // Extract and show video thumbnail
                            val bitmap = Constant.getVideoThumbnail(this, uri!!)
                            binding.thumbnailView.setImageBitmap(bitmap)
                            binding.thumbnailView.visibility = View.VISIBLE
                            binding.imgDelete.visibility = View.VISIBLE
                            binding.imgPlay.visibility = View.VISIBLE
                            binding.videoView.setVideoURI(uri)
                            binding.videoView.setMediaController(MediaController(this))
                            binding.videoView.requestFocus()
                        } else {
                            binding.videoView.visibility = View.GONE
                            binding.imgDelete.visibility = View.GONE
                            binding.thumbnailView.visibility = View.GONE
                            binding.rcyImages.visibility = View.VISIBLE
                            mAdapter?.notifyDataSetChanged()
                        }

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

        binding.imgDelete.setOnClickListener {
            binding.videoView.visibility = View.GONE
            binding.imgDelete.visibility = View.GONE
            binding.imgPlay.visibility = View.GONE
            binding.rcyImages.visibility = View.VISIBLE
            binding.thumbnailView.visibility = View.GONE
            Constant.selectedFiles.clear()
            saveDrawableToCache(R.drawable.add_image)?.let {
                Constant.selectedFiles.add(
                    FileItem(
                        it, FileType.IMAGE
                    )
                )
            }
            mAdapter!!.notifyDataSetChanged()
        }

        binding.imgPlay.setOnClickListener {
            binding.thumbnailView.visibility = View.GONE
            binding.imgPlay.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            binding.videoView.start()
        }

        binding.txtTitle.filters = arrayOf(InputFilter.LengthFilter(Constant.isTitleLength))
        binding.txtDesc.filters = arrayOf(InputFilter.LengthFilter(Constant.isDescriptionLength))
        Constant.editTextCounter(
            this,
            binding.txtDesc,
            Constant.isDescriptionLength,
            binding.lbTextCount
        )
        Constant.editTextCounter(
            this,
            binding.txtTitle,
            Constant.isTitleLength,
            binding.lbtitleTextCount
        )


        appViewModel?.IsGetEventSchoolReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed
                updateVisibility(
                    allOngoingEvents,
                    binding.rcyongoingevent,
                    binding.headerview,
                    binding.dotindicator
                )
                updateVisibility(
                    allUpcomingEvents, binding.rcyupcomingevent, binding.upcomingeventHeaderview
                )
                updateVisibility(
                    allCompletedEvents, binding.rcycompletedevent, binding.completedeventHeaderview
                )

                isloadeventData(allOngoingEvents)
                isloadUpcomingData(allUpcomingEvents)
                isloadCompletedData(allCompletedEvents)

            } else {
                hideAllSections()
            }
        }

    }


    private fun <T> updateVisibility(
        dataList: List<T>?, recyclerView: RecyclerView, vararg headers: View
    ) {
        if (!dataList.isNullOrEmpty()) {
            recyclerView.visibility = View.VISIBLE
            headers.forEach { it.visibility = View.VISIBLE }
        } else {
            recyclerView.visibility = View.GONE
            headers.forEach { it.visibility = View.GONE }
        }
    }


    private fun hideAllSections() {
        updateVisibility(
            emptyList<Any>(), binding.rcyongoingevent, binding.headerview, binding.dotindicator
        )
        updateVisibility(
            emptyList<Any>(), binding.rcyupcomingevent, binding.upcomingeventHeaderview
        )
        updateVisibility(
            emptyList<Any>(), binding.rcycompletedevent, binding.completedeventHeaderview
        )
    }


    private fun loadeventdata() {
        schooleventAdapter = SchoolEventAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyongoingevent.isNestedScrollingEnabled = false
        binding.rcyongoingevent.adapter = mAdapter

        eventupcomingadapter = SchoolEventUpcomingAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcyupcomingevent.isNestedScrollingEnabled = false
        binding.rcyupcomingevent.adapter = eventupcomingadapter


        eventcompletedadapter =
            SchoolEventCompletedAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcycompletedevent.isNestedScrollingEnabled = false
        binding.rcycompletedevent.adapter = eventcompletedadapter



        appViewModel!!.IsGetEventSchoolReport(isAccessToken!!, this)
    }


    private fun isloadeventData(newData: List<SchoolEventItem>?) {
        schooleventAdapter = SchoolEventAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.adapter = schooleventAdapter
    }


    private fun isloadUpcomingData(newData: List<SchoolEventItem>?) {
        eventupcomingadapter =
            SchoolEventUpcomingAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.adapter = eventupcomingadapter
    }

    private fun isloadCompletedData(newData: List<SchoolEventItem>?) {
        eventcompletedadapter =
            SchoolEventCompletedAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.adapter = eventcompletedadapter
    }



    override fun onSearchResultEmpty(adapterTag: String, isEmpty: Boolean) {
        when (adapterTag) {
            "ONGOING" -> binding.rcyongoingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            "COMPLETED" -> binding.rcycompletedevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            "UPCOMING" -> binding.rcyupcomingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    override fun onDeleteEvent(type: String?, id: String?, position: Int)  {
        val json = JSONObject()
        json.put("id", id)
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        appViewModel?.isEventDelete(isAccessToken!!, requestBody, this)

        appViewModel!!.isEventDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@CreateEvent)

                    eventupcomingadapter.removeItemAt(position)

                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail),
                        response.message,
                        this
                    )
                }
            }
        }
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
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Camera permission is permanently denied. Please enable it from app settings.")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun openAlbumSelectActivity(isFileType: String) {

        if (Constant.selectedFiles.size > 1) {
            val secondType = Constant.selectedFiles[1].type.toString()
            if (
                (secondType == Constant.IMAGE && (isFileType == Constant.DOCUMENT || isFileType == Constant.VOICE)) ||
                (secondType == Constant.DOCUMENT && (isFileType == Constant.IMAGE || isFileType == Constant.VOICE)) ||
                (secondType == Constant.VOICE && (isFileType == Constant.IMAGE || isFileType == Constant.DOCUMENT))
            ) {
                Constant.selectedFiles.clear()
                saveDrawableToCache(R.drawable.add_image)?.let {
                    Constant.selectedFiles.add(FileItem(it, FileType.IMAGE))
                }
                mAdapter?.notifyDataSetChanged()
            }
        }
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

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.lnrTabOneName -> {
                binding.eventCreate.visibility = View.VISIBLE
                binding.line1.setBackgroundResource(R.color.iconBlue)
                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.line2.setBackgroundResource(R.color.white)
                binding.scrollContainer.visibility = View.GONE
                binding.rytRecyclewview.visibility = View.VISIBLE

            }

            R.id.lnrTabTwoName -> {
                binding.eventCreate.visibility = View.GONE
                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
                binding.line2.setBackgroundResource(R.color.iconBlue)
                binding.line1.setBackgroundResource(R.color.white)
                binding.scrollContainer.visibility = View.VISIBLE
                binding.rytRecyclewview.visibility = View.GONE
                loadeventdata()
            }
            R.id.imgBack -> {
                Constant.selectedFiles.clear()
                Constant.isAwsUploadedFiles.clear()
                onBackPressed()
            }

            R.id.rytStartDate-> {

                selectedDateField = 1
                Constant.showDatePicker(this, false) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text =
                        Constant.covertDateFormate(selectedDate) // 13 may 2222
                    val (day, formattedDate) = Constant.getDayAndDateOnly(binding.txtStartDate.text.toString())// 13 Mon
                    binding.lblDay.text = formattedDate
                    binding.lblDate.text = day
                }
            }


            R.id.txtStartTime -> {
                isFromTime = true
                showTimePickerDialog(this, this)

            }

            R.id.btnNext -> {
                RedirectToRecepientActivity()
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
            Constant.isFileLimit = 1
            openAlbumSelectActivity(Constant.VIDEO)
            dialog.dismiss()
        }

        rlaDocument.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.DOCUMENT)
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {
            if (Constant.selectedFiles.size > 1) {
                if (Constant.selectedFiles[1].type.toString() != Constant.IMAGE) {
                    Constant.selectedFiles.clear()
                    saveDrawableToCache(R.drawable.add_image)?.let {
                        Constant.selectedFiles.add(
                            FileItem(
                                it, FileType.IMAGE
                            )
                        )
                    }
                    mAdapter!!.notifyDataSetChanged()
                }
            }
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
            Toast.makeText(this, "Max ${MAX_FILES} files allowed", Toast.LENGTH_SHORT)
                .show()
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
                    ".xlsx",
                    true
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


    override fun onClickListener(data: CreateEvent) {
        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.add_image)?.let {
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

        val eventDetails = EventDetails(txtLocation, txtTitle, txtDesc, txtStartDate, txtStartTime)

//        if (Constant.selectedFiles.isNotEmpty()) {
//            Constant.selectedFiles.removeAt(0)
//        }

        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.event_data, eventDetails)
        startActivity(intent)
    }
}