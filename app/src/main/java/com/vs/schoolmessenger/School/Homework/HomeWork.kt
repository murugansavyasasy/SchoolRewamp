package com.vs.schoolmessenger.School.Homework

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeWorkBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeWork : BaseActivity<HomeWorkBinding>(), View.OnClickListener, OnImageClickListener,
    OnDateSelectedListener, HomeWorkReportClickListener {

    override fun getViewBinding(): HomeWorkBinding {
        return HomeWorkBinding.inflate(layoutInflater)
    }

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        private const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    var isAcademicYear: List<AcademicYear>? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    private var isStaffDetails: StaffDetails? = null
    var isSection: List<Section>? = null
    var isGetStandard: List<Standard>? = null
    private lateinit var isHomeWorkReportData: List<HomeWorkReport>
    var mHomeWorkReportAdapter: HomeWorkReportAdapter? = null
    var isSectionId = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.lblDatePick.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.AcademicYear.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        binding.Calendar.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.HomeWork)
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

        binding.selectdate.text = Constant.getCurrentDate()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year == true } == true
                binding.lblAcademicYear.text = isAcademicYear!![0].year
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                isGetStandardSection()
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                        binding.lblStandard.text = isGetStandard!!.get(0).name
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            binding.lblSection.text = isGetStandard!![0].sections.get(0).name
                            isSection = isGetStandard!!.get(0).sections
                            fetchHomeWorkReportData()
                        }
                    } else {
                        binding.rlaStandard.visibility = View.GONE
                        binding.rlaSection.visibility = View.GONE
                    }
                }
            }
        }

        appViewModel!!.isGetHomeWorkReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcyHomeWorkReport.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    val isHomeWorkReport = response.data
                    isHomeWorkReportData = isHomeWorkReport
                    loadHomeWorkReportData(isHomeWorkReportData)
                } else {
                    binding.rcyHomeWorkReport.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text = response.message
                }
            }
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

    fun saveDrawableToCache(drawableResId: Int): String? {
        val drawable = ContextCompat.getDrawable(this, drawableResId) ?: return null
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                Constant.selectedFiles.clear()
                Constant.isAwsUploadedFiles.clear()
                onBackPressed()
            }

            R.id.rlaStandard -> {
                showStandardDropdown(
                    binding.rlaStandard, this, isGetStandard
                ) { selectStandard, position ->
                    binding.lblStandard.text = selectStandard.name
                    binding.lblSection.text = selectStandard.sections[0].name
                    isSectionId = selectStandard.sections[0].id
                    isSection = selectStandard.sections
                    Log.d(
                        "DropdownMenu",
                        "Selected Standard: Name = ${selectStandard.name}, ID = ${selectStandard.id}, Position = $position"
                    )
                    fetchHomeWorkReportData()
                }
            }

            R.id.rlaSection -> {
                isDropDownLoadDataSection(
                    binding.lblSection, this, isSection
                ) { selectedOption ->
                    binding.lblSection.text = selectedOption.first
                    isSectionId = selectedOption.second
                    fetchHomeWorkReportData()
                }
            }

            R.id.lblDatePick -> {
                showDatePickerDialog(this, this)
                fetchHomeWorkReportData()
            }

            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rlaHomeWorkReport.visibility = View.GONE
                binding.rlaHomework.visibility = View.VISIBLE
            }

            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rlaHomeWorkReport.visibility = View.VISIBLE
                binding.rlaHomework.visibility = View.GONE
                isGetAcademicYear()
            }

            R.id.btnChooseRecipient -> {
                RedirectToSectionStudents()
            }
            R.id.Calendar -> {
                fetchHomeWorkReportData()
            }
            R.id.AcademicYear -> {
                showAcademicDropdown(
                    binding.AcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    isGetStandardSection()
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
                    fetchHomeWorkReportData()
                }
            }
        }
    }

    private fun fetchHomeWorkReportData() {
        binding.rcyHomeWorkReport.visibility = View.VISIBLE
        mHomeWorkReportAdapter = HomeWorkReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomeWorkReport.layoutManager = LinearLayoutManager(this)
        binding.rcyHomeWorkReport.isNestedScrollingEnabled = false
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter
        appViewModel?.isGetHomeWorkReport(
            isAccessToken!!, isSectionId, isAcademicYearId, binding.selectdate.text.toString(), this
        )
    }

    private fun loadHomeWorkReportData(isHomeWorkReportDetails: List<HomeWorkReport>) {
        binding.rcyHomeWorkReport.visibility = View.VISIBLE
        mHomeWorkReportAdapter = HomeWorkReportAdapter(
            isHomeWorkReportDetails, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyHomeWorkReport.layoutManager = LinearLayoutManager(this)
        binding.rcyHomeWorkReport.isNestedScrollingEnabled = false
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter

    }


    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }


    private fun RedirectToSectionStudents() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        if (title.isEmpty()) {
            binding.edtTitle.error = getString(R.string.Title_required)
            binding.edtTitle.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.edtDescription.error = getString(R.string.Title_required)
            binding.edtDescription.requestFocus()
            return
        }
//        if (Constant.selectedFiles.size == 1) {
//            Toast.makeText(this, "Choose atleast one file", Toast.LENGTH_SHORT).show()
//            return
//        }
        val sectionDetails = SectionDetails(title, description)
        Constant.selectedFiles.removeAt(0)
        Log.d("Constant.selectedFiles", Constant.selectedFiles.toString())
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.section_data, sectionDetails)
        startActivity(intent)
    }


    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)
        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)

        rlaGallery.setOnClickListener {
            onImageButtonClick()
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()

            dialog.dismiss()
        }
        rlaDocument.setOnClickListener {
            onPdfButtonClick()
            dialog.dismiss()
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ) // Size
            setGravity(Gravity.BOTTOM) // Display at the bottom
            setWindowAnimations(R.style.PopupAnimation) // Apply the animation
        }
        dialog.show()

    }

    fun onImageButtonClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST)
    }

    fun onPdfButtonClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)  // Important for file-only types
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Documents"), PICK_DOCUMENT_REQUEST
        )
    }

    private fun copyDocumentToInternalStorage(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "copied_document.pdf") // Save to app's internal storage

        try {
            inputStream?.copyTo(FileOutputStream(file))
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }

        return null
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
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
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
            Toast.makeText(this, "Max $MAX_FILES files allowed", Toast.LENGTH_SHORT).show()
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
            PICK_IMAGE_REQUEST, PICK_DOCUMENT_REQUEST -> {
                data?.clipData?.let { cd ->
                    val toTake = minOf(cd.itemCount, remaining)
                    for (i in 0 until toTake) {
                        val uri = cd.getItemAt(i).uri
                        addPath(uri) // Use the updated addPath that handles MIME type
                        if (uri.toString().contains("document")) copyDocumentToInternalStorage(uri)
                    }

                    if (cd.itemCount > remaining) Toast.makeText(
                        this, "Only $remaining added", Toast.LENGTH_SHORT
                    ).show()
                } ?: data?.data?.let { uri ->

                    addPath(uri) // Use the updated addPath that handles MIME type
                    if (uri.toString().contains("document")) copyDocumentToInternalStorage(uri)
                }
            }

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
                result = result?.substring(cut + 1)
            }
        }
        return result ?: ""
    }

    private fun getFilePathForDocument(filePath: String): String? {
        // Handle converting document path to actual file path if possible.
        // For example, for a PDF, the file might be stored in external storage,
        // so ensure you use the correct path conversion logic here if needed.
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

    override fun onDateSelected(date: String) {
        binding.selectdate.text = date
    }

    private fun isBackRoundChange(isClickingId: TextView) {
        binding.lytNoDataFound.visibility = View.GONE
        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }


        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_light_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.white_bg_radius)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.black))

    }
    override fun onClickListener(data: HomeWorkReport) {
        Constant.isAwsUploadedFiles.clear()
        Constant.selectedFiles.clear()
        saveDrawableToCache(R.drawable.add_image)?.let {
            Constant.selectedFiles.add(
                FileItem(
                    it, FileType.IMAGE
                )
            )
        }
        isBackRoundChange(binding.btnCreate)
        binding.rlaHomeWorkReport.visibility = View.GONE
        binding.rlaHomework.visibility = View.VISIBLE
        binding.edtTitle.setText(data.title)
        binding.edtDescription.setText(data.description)

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
}