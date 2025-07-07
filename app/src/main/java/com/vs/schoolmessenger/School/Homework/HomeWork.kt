package com.vs.schoolmessenger.School.Homework

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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.MediaController
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeWorkBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeWork : BaseActivity<HomeWorkBinding>(), View.OnClickListener, OnImageClickListener,
    OnDateSelectedListener, HomeWorkReportClickListener {

    override fun getViewBinding(): HomeWorkBinding {
        return HomeWorkBinding.inflate(layoutInflater)
    }

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
    }

    private var cameraPermissionDeniedCount = 0

    private val CAMERA_IMAGE_REQUEST = 1001
    var isFirstLoad = false
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
    private var isHomeWorkReportData: List<HomeWorkReport>? = null
    var mHomeWorkReportAdapter: HomeWorkReportAdapter? = null
    private var fullHomeworkList: List<HomeWorkReport> = listOf()
    var isSectionId = -1
    var isAcademicServerLoad=false
    var isSelectedDate=""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.AcademicYear.setOnClickListener(this)
        binding.btnChooseRecipient.setOnClickListener(this)
        binding.Calendar.setOnClickListener(this)
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
        mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
        binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
        binding.rcyImages.adapter = mAdapter

        isSelectedDate=Constant.getCurrentDate()
        binding.selectdate.text = Constant.convertToReadableDate(Constant.getCurrentDate())


        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isGetStandardSection()

        binding.edtTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.lblTitleTextCount.text = "$length/50"
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.edtDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.lblTextCount.text = "$length/500"
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        binding.rytStandardDropDown.visibility = View.VISIBLE
                        binding.rytSectionDropDown.visibility = View.VISIBLE
                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            isLoadStandard(isGetStandard)
                            isSection = isGetStandard!!.get(0).sections
                        }
                    } else {
                        binding.rytStandardDropDown.visibility = View.GONE
                        binding.rytSectionDropDown.visibility = View.GONE
                    }
                }
            }
        }

        appViewModel!!.isGetHomeWorkReport?.observe(this) { response ->
            if (response != null) {
                isFirstLoad = true
                if (response.status) {
                    binding.rcyHomeWorkReport.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    binding.search.visibility=View.VISIBLE
                    binding.line1.visibility=View.VISIBLE
                    binding.line2.visibility=View.VISIBLE
                    val isHomeWorkReport = response.data
                    isHomeWorkReportData = isHomeWorkReport
                    loadHomeWorkReportData(isHomeWorkReportData!!)
                } else {
                    binding.search.visibility=View.GONE
                    binding.line1.visibility=View.GONE
                    binding.line2.visibility=View.GONE
                    binding.rcyHomeWorkReport.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text = response.message
                }
            }
        }

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
                            this, "Only $remaining files added (max $MAX_FILES)", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        binding.imgDelete.setOnClickListener {
            binding.videoView.visibility = View.GONE
            binding.webView.visibility = View.GONE
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

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase(Locale.ROOT)
                filterHomeWorkReport(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                if (isFirstLoad) {
                    val selectedOption = isAcademicYear!![position]
                    isAcademicYearId = selectedOption.id
                    isCurrentAcademicYear = selectedOption.current_academic_year
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                    )
                    fetchHomeWorkReportData()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadStandard(isStandard: List<Standard>?) {
        val adapter = StandardDropDownListAdapter(this, isStandard)
        binding.isSpinnerStandard.adapter = adapter
        binding.isSpinnerStandard.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isStandard!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard[position].id}, Year = ${isStandard[position].name}"
                    )

                    isSectionId = isStandard[position].id
                    isSection = isStandard[position].sections
                    isLoadSection(isSection)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isLoadSection(isSection: List<Section>?) {
        val adapter = SectionDropDownListAdapter(this, isSection)
        binding.isSpinnerSection.adapter = adapter
        binding.isSpinnerSection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isSection!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isSection[position].id}, Year = ${isSection[position].name}"
                    )
                    isSectionId = selectedOption.id
                    fetchHomeWorkReportData()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun filterHomeWorkReport(query: String) {
        val lowerQuery = query.lowercase(Locale.getDefault())

        val filteredList = if (query.isEmpty()) {
            fullHomeworkList
        } else {
            fullHomeworkList.filter {
                it.title.lowercase(Locale.getDefault())
                    .contains(lowerQuery) || it.description.lowercase(Locale.getDefault())
                    .contains(lowerQuery) || it.subject_name.lowercase(Locale.getDefault())
                    .contains(lowerQuery)
            }
        }

        mHomeWorkReportAdapter = HomeWorkReportAdapter(filteredList, this, this, false)
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter

        if (filteredList.isEmpty()) {
            binding.rcyHomeWorkReport.visibility = View.GONE
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.noDataFound.text = getString(R.string.no_matching_homework_found)
        } else {
            binding.rcyHomeWorkReport.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
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

            R.id.Calendar -> {
                showDatePickerDialogSelectedDate(this,isSelectedDate, this)

            }

            R.id.btnCreate -> {
                binding.btnCreate.isEnabled=false
                binding.btnHistory.isEnabled=true
                isBackRoundChange(binding.btnCreate)
                binding.rlaHomeWorkReport.visibility = View.GONE
                binding.rlaHomework.visibility = View.VISIBLE
                //Now once it is tab is swapped the academic year is already assigned so fetchHomeWorkReportData() will not be call
                //So we are handling it by make it isAcademicServerLoad as true
                isAcademicServerLoad=true
            }

            R.id.btnHistory -> {
                binding.btnHistory.isEnabled=false
                binding.btnCreate.isEnabled=true
                isBackRoundChange(binding.btnHistory)
                binding.rlaHomeWorkReport.visibility = View.VISIBLE
                binding.rlaHomework.visibility = View.GONE
                //At initial swap we are avoiding the fetchHomeWorkReportData because Academic Year dropdown is doing fetchHomeWorkReportData
                if (isAcademicServerLoad){
                fetchHomeWorkReportData()
                }
            }

            R.id.btnChooseRecipient -> {
                isRedirectToSectionStudents()
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
            isAccessToken!!, isSectionId, isAcademicYearId, isSelectedDate, this
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

    private fun isRedirectToSectionStudents() {
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

        val sectionDetails = SectionDetails(title, description)
//        Constant.selectedFiles.removeAt(0)
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.section_data, sectionDetails)
        startActivity(intent)
    }

    private fun openAlbumSelectActivity(isFileType: String) {

        if (Constant.selectedFiles.size > 1) {
            val secondType = Constant.selectedFiles[1].type.toString()
            if ((secondType == Constant.IMAGE && (isFileType == Constant.DOCUMENT || isFileType == Constant.VOICE)) || (secondType == Constant.DOCUMENT && (isFileType == Constant.IMAGE || isFileType == Constant.VOICE)) || (secondType == Constant.VOICE && (isFileType == Constant.IMAGE || isFileType == Constant.DOCUMENT))) {
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
            Constant.isFileLimit = 5
            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            Constant.isFileLimit = 1
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            Constant.isFileLimit = 1
            openAlbumSelectActivity(Constant.VIDEO)
            dialog.dismiss()
        }

        rlaDocument.setOnClickListener {
            Constant.isFileLimit = 5
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
                    this, "${applicationContext.packageName}.fileprovider", photoFile
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
            Toast.makeText(this, "Max $MAX_FILES files allowed", Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSelected(date: String) {
        isSelectedDate=date
        if (Constant.getCurrentDate()==isSelectedDate){
            binding.lblDateFormat.text=getString(R.string.today)
        }
        else{
            binding.lblDateFormat.text=getString(R.string.past_date)
        }
        binding.selectdate.text = Constant.convertToReadableDate(date)
        fetchHomeWorkReportData()
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
        binding.webView.visibility = View.GONE
        binding.thumbnailView.visibility = View.GONE
        binding.rcyImages.visibility = View.GONE
        binding.thumbnailView.visibility = View.GONE
        binding.imgDelete.visibility = View.GONE

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
        if (Constant.selectedFiles[1].type.toString() == Constant.VIDEO) {
            binding.thumbnailView.visibility = View.GONE
            binding.webView.visibility = View.VISIBLE
            binding.rcyImages.visibility = View.GONE
            binding.thumbnailView.visibility = View.GONE
            binding.imgDelete.visibility = View.VISIBLE
            loadVideo(binding.webView, Constant.selectedFiles.get(1).path.toString())
        } else {
            binding.rcyImages.visibility = View.VISIBLE
            mAdapter = ImagePickingAdapter(this, Constant.selectedFiles, this)
            binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
            binding.rcyImages.adapter = mAdapter
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadVideo(webView: android.webkit.WebView, url: String) {
        binding.loadingBar.visibility = View.VISIBLE
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: android.webkit.WebView?, url: String?, favicon: Bitmap?
            ) {
                binding.loadingBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                binding.loadingBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: android.webkit.WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                binding.loadingBar.visibility = View.GONE
                Log.e("WebViewError", "Error loading: ${error?.description}")
            }
        }
        webView.loadUrl(url)
    }
}