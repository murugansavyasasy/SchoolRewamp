package com.vs.schoolmessenger.School.Event

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.Model.EventDetails
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.CreateEventBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEvent : BaseActivity<CreateEventBinding>(),OnImageClickListener,
    View.OnClickListener, OnDateSelectedListener, EventClickListener, TimeSelectedListener {

    override fun getViewBinding(): CreateEventBinding {
        return CreateEventBinding.inflate(layoutInflater)
    }

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }


    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    var isFromTime = true
    private var isStaffDetails: StaffDetails? = null
    private var selectedDateField: Int = 0

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.rytStartDate.setOnClickListener(this)
        binding.rytStart.setOnClickListener(this)
        binding.rytStartTime.setOnClickListener(this)
        binding.lnrStartCalendar.setOnClickListener(this)
        binding.txtStartDate.setOnClickListener(this)
        binding.txtStartTime.setOnClickListener(this)

        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = "Create Event"
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

        val (dayOnly, dayOfWeek, fullDate, slashDate) = Constant.getCurrentDateInfo()
        binding.lblDate.text = dayOnly
        binding.lblDay.text = dayOfWeek

        binding.txtStartDate.text = fullDate
        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                    val remaining = Companion.MAX_FILES - Constant.selectedFiles.size

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
                        Log.d("SelectedFile", "URI: $uri, Type: $type")
                    }

                    if ((selectedUris?.size ?: 0) > remaining) {
                        Toast.makeText(
                            this,
                            "Only $remaining files added (max ${MAX_FILES})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    mAdapter?.notifyDataSetChanged()
                }
            }

        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)
        Constant.editTextCounter(this,binding.txtTitle,50,binding.lbtitleTextCount)

    }

    override fun onResume() {
        super.onResume()
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

            R.id.rytStartDate,R.id.txtStartDate,R.id.lnrStartCalendar -> {
                selectedDateField = 1
                Constant.showDatePicker(this) { selectedDate ->
                    Log.d("selectedDate", selectedDate)
                    binding.txtStartDate.text = Constant.covertDateFormate(selectedDate) // 13 may 2222
                    val parts = binding.txtStartDate.text.split(" ")
                    val day = parts[0]
                    val Date = parts[1]
                    binding.lblDay.text = day
                    binding.lblDate.text = Date
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

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        super.onBackPressed()
    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }

    private fun openAlbumSelectActivity(isFileType: String) {
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
            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
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
            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            openAlbumSelectActivity(Constant.VIDEO)
            dialog.dismiss()
        }

        rlaDocument.setOnClickListener {
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


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this, "${applicationContext.packageName}.fileprovider", it
                )
                cameraImageFilePath = it.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, HomeWork.Companion.CAMERA_IMAGE_REQUEST)
            } ?: Toast.makeText(this, "Could not create file for photo", Toast.LENGTH_SHORT).show()
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
            if (Constant.selectedFiles!!.size >= MAX_FILES) return

            // Skip only audio and video
            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) {
                Log.d("SkipFile", "Skipping audio/video file: $uri (MIME: $mimeType)")
                return
            }

            // Resolve path (optional, based on your needs)
            val path: String? = when {
                uri.scheme == "file" -> uri.path
                else -> getPathFromUri(uri)
            }

            if (path == null) {
                Log.w("addPath", "Could not resolve path from URI: $uri")
                return
            }
            val fileName = getFileName(uri).ifEmpty { File(path).name }
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
            for (item in Constant.selectedFiles!!) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
            }
        }

        when (requestCode) {
            HomeWork.Companion.CAMERA_IMAGE_REQUEST -> {
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
        }
        mAdapter!!.notifyDataSetChanged()
    }

    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null

        if (uri.scheme.equals("content", ignoreCase = true)) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("_data")
                    if (columnIndex != -1) {
                        path = it.getString(columnIndex)
                    }
                }
            }
        }

        if (path == null && DocumentsContract.isDocumentUri(this, uri)) {
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                try {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val filePath = split[1]

                    // Handle specific file type based on URI
                    path = getFilePathForDocument(filePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return path
    }




    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result ?: ""
    }

    private fun getFilePathForDocument(filePath: String): String? {
        return filePath // This is just a placeholder; implement appropriate logic for your use case.
    }



    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = cacheDir // Or getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMG_${timeStamp}_",  /* prefix */
            ".jpg",               /* suffix */
            storageDir            /* directory */
        )
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



        Log.d("RedirectToRecepientActivity", "Location: $txtLocation")
        Log.d("RedirectToRecepientActivity", "Title: $txtTitle")
        Log.d("RedirectToRecepientActivity", "Start Date: $txtStartDate")
        Log.d("RedirectToRecepientActivity", "Description: $txtDesc")
        Log.d("RedirectToRecepientActivity", "Start Time: $txtStartTime")

        if (txtLocation.isEmpty()) {
            Log.d("RedirectToRecepientActivity", "Location is empty")
            binding.txtLocation.error = "Location is required"
            binding.txtLocation.requestFocus()
            return
        }

        if (txtTitle.isEmpty()) {
            Log.d("RedirectToRecepientActivity", "Description is empty")
            binding.txtTitle.error = getString(R.string.Title_required)
            binding.txtTitle.requestFocus()
            return
        }

        if (txtDesc.isEmpty()) {
            Log.d("RedirectToRecepientActivity", "Description is empty")
            binding.txtDesc.error = "Description is required"
            binding.txtDesc.requestFocus()
            return
        }

        val eventDetails = EventDetails(txtLocation, txtTitle, txtDesc, txtStartDate, txtStartTime)
        Log.d("RedirectToRecepientActivity", "NoticeBoardDetails created: $eventDetails")

        if (Constant.selectedFiles.isNotEmpty()) {
            Log.d(
                "RedirectToRecepientActivity",
                "Removing first file from selectedFiles: ${Constant.selectedFiles[0]}"
            )
            Constant.selectedFiles.removeAt(0)
        } else {
            Log.d("RedirectToRecepientActivity", "selectedFiles list is already empty")
        }

        Log.d("RedirectToRecepientActivity", "Remaining selectedFiles: ${Constant.selectedFiles}")

        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.event_data, eventDetails)
        Log.d("RedirectToRecepientActivity", "Starting RecepientActivity with event data")
        startActivity(intent)

    }

}