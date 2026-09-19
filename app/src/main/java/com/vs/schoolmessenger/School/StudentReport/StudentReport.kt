package com.vs.schoolmessenger.School.StudentReport

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.StudentReportBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
    StudentReportClickListener {

    private var appViewModel: App? = null
    private lateinit var studentreportadapter: StudentReportAdapter
    private var isAccessToken: String? = null
    var isSection: List<Section>? = null
    var isValidAcademicYear = false
    var isAcademicYear: List<AcademicYear>? = null
    private var isStaffDetails: StaffDetails? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isSectionId = -1
    var isGetStandard: List<Standard>? = null
    private var isClassID: Int? = null
    private var isSectionID: Int? = null
    private var filterSelectedOption: String? = null
    private var hasUserSelectedSection = false
    private var hasUserSelectedStandard = false
    var isAllSectionSelected = true
    private var hasAcademicYearManuallyChanged = false
    private lateinit var filterCaterotyType: List<String>
    private var originalStudentList: List<StudentReportData> = listOf()
    private var currentFilteredList: List<StudentReportData> = listOf()
    private var currentSortType: SortType? = null

    private lateinit var genderSpinnerAdapter: SpinnerLoadingAdapter

    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    private var selectedStudentForProfileUpdate: StudentReportData? = null
    private var pendingProfileImageUri: Uri? = null
    private var singleImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private val CAMERA_IMAGE_REQUEST = 1004
    private var cameraPermissionDeniedCount = 0

    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        filterCaterotyType = listOf(
            resources.getString(R.string.all_student),
            resources.getString(R.string.class_and_section)
        )
        val filterGenderCaterotyType = listOf(
            getString(R.string.all),
            getString(R.string.male),
            getString(R.string.female),
            getString(R.string.lblOthers),
        )

        setupFilerCatoryTypeSpinner()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearchBar.isVisible) {
                binding.rytSearchBar.visibility = View.GONE
                binding.txtSearchMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)
            } else {
                binding.rytSearchBar.visibility = View.VISIBLE
                binding.txtSearchMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)
            }
        }

        binding.imgDelete.setOnClickListener(this)
        binding.tapNameAsc.setOnClickListener(this)
        binding.tapNoDsc.setOnClickListener(this)
        binding.tapRollAsc.setOnClickListener(this)
        binding.tapRollDsc.setOnClickListener(this)
        binding.tapNameDsc.setOnClickListener(this)
        binding.tapNoAsc.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        Log.d("isAccessToken", isStaffDetails!!.access_token)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        setupGenderCaterotyType(filterGenderCaterotyType)

        isLoadAcademicYear(isAcademicYearList)
        isValidAcademicYear =
            isAcademicYearList?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYearList!![0].id
        isCurrentAcademicYear = isAcademicYearList!![0].current_academic_year
        Log.d("isAcademicYearId", isAcademicYearId.toString())
        hasAcademicYearManuallyChanged = false
        if (filterSelectedOption != null) {
            isGetStandardSection()
        }
        binding.rlaStandardPicking.visibility = View.VISIBLE

        appViewModel!!.isStudentReportList?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    ShowData()
                    loadStudentReport(response.data)
                } else {
                    originalStudentList = emptyList()
                    currentFilteredList = emptyList()
                    studentreportadapter.updateData(emptyList())
                    binding.tabLayout.visibility = View.GONE
                    ErrorMessage(response.message)
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearchBar.visibility = View.GONE
                }
                val mobileNumber = SharedPreference.getMobileNumber(this)
                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.mobile_number, mobileNumber)
                    addProperty(APIKeyNames.activity, Constant.add_points_view_student_report)
                    addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                    addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                }
                appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

            } else {
                originalStudentList = emptyList()
                currentFilteredList = emptyList()
                binding.tabLayout.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytSearchBar.visibility = View.GONE
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        binding.rlaStandardPicking.visibility = View.VISIBLE
                        isClassID = isGetStandard!!.get(0).id
                        isAllSectionSelected = true
                        isLoadStandard(isGetStandard)
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            isSection = isGetStandard!!.get(0).sections
                        }
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        binding.rytSearchBar.visibility = View.GONE
                        isGetStudentReport()
                    } else {
                        originalStudentList = emptyList()
                        currentFilteredList = emptyList()
                        binding.tabLayout.visibility = View.GONE
                        binding.rlaStandardPicking.visibility = View.GONE
                        ErrorMessage(response.message)
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        binding.rytSearchBar.visibility = View.GONE
                    }
                }
            } else {
                originalStudentList = emptyList()
                currentFilteredList = emptyList()
                binding.tabLayout.visibility = View.GONE
                binding.rlaStandardPicking.visibility = View.GONE
                ErrorMessage(getString(R.string.something_went_wrong_please_try_again_later))
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytSearchBar.visibility = View.GONE
            }
        }

        appViewModel!!.isupdateporiflestudent?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null && response.status) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                isGetStudentReport()
            } else {
                Toast.makeText(
                    this,
                    response?.message ?: getString(R.string.Something_went_wrong_Please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
            }
            pendingProfileImageUri = null
            selectedStudentForProfileUpdate = null
        }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextSSS", s.toString())
                filter(s.toString())
            }
        })

        singleImageLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                onProfileImageSelected(uri)
            }
        }
    }

    private fun isGetStudentReport() {
        binding.txtSearchMenu.text.clear()
        studentreportadapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = studentreportadapter

        if (getString(R.string.all_student) == filterSelectedOption) {
            binding.rlaStandardPicking.visibility = View.GONE
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!, isAcademicYearId, activity = this
            )
        }

        if (getString(R.string.class_and_section) == filterSelectedOption) {
            binding.rlaStandardPicking.visibility = View.VISIBLE
            if (isAllSectionSelected) {
                appViewModel!!.getStudentReportDetails(
                    isAccessToken!!, isAcademicYearId, class_id = isClassID!!, activity = this
                )
            } else {
                appViewModel!!.getStudentReportDetails(
                    isAccessToken!!,
                    isAcademicYearId,
                    class_id = isClassID!!,
                    section_id = isSectionID!!,
                    activity = this
                )
            }
        }
        if (filterSelectedOption == null) {
            Log.d("filterSelectedOption", "filterSelectedOption is null")
        }
    }

    private fun sortList(sortType: SortType) {
        currentSortType = sortType
        val sortedList = when (sortType) {
            SortType.ROLL_ASC -> currentFilteredList.sortedWith(
                compareBy(Constant.naturalComparator) { it.roll_no }
            )
            SortType.ROLL_DESC -> currentFilteredList.sortedWith(
                compareByDescending(Constant.naturalComparator) { it.roll_no }
            )
            SortType.NO_ASC -> currentFilteredList.sortedWith(
                compareBy(Constant.naturalComparator) { it.admission_no }
            )
            SortType.NO_DESC -> currentFilteredList.sortedWith(
                compareByDescending(Constant.naturalComparator) { it.admission_no }
            )
            SortType.NAME_ASC -> currentFilteredList.sortedBy { it.name?.lowercase() }
            SortType.NAME_DESC -> currentFilteredList.sortedByDescending { it.name?.lowercase() }
        }
        studentreportadapter.updateData(sortedList)

        if (binding.txtSearchMenu.text.isNotEmpty()) {
            filter(binding.txtSearchMenu.text.toString())
        } else {
            binding.txtSearchMenu.text.clear()
        }
    }

    private fun filterByGender(genderType: GenderType) {
        currentFilteredList = when (genderType) {
            GenderType.ALL -> originalStudentList
            GenderType.MALE -> originalStudentList.filter { it.gender.equals("Male", true) }
            GenderType.FEMALE -> originalStudentList.filter { it.gender.equals("Female", true) }
            GenderType.OTHERS -> originalStudentList.filter { it.gender.equals("Others", true) }
        }

        if (currentFilteredList.isEmpty()) {
            ErrorMessage(getString(R.string.no_student_found))
        } else {
            ShowData()
            studentreportadapter.updateData(currentFilteredList)
        }
    }

    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
        if (studentReportData.isNullOrEmpty()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytSearchBar.visibility = View.GONE
        } else {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rytSearchBar.visibility = View.GONE
            originalStudentList = studentReportData
            currentFilteredList = originalStudentList

            studentreportadapter =
                StudentReportAdapter(currentFilteredList, this, this, Constant.isShimmerViewDisable)
            binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
            binding.rcyStudentReport.adapter = studentreportadapter
            genderSpinnerAdapter.selectedPosition = 0
            genderSpinnerAdapter.notifyDataSetChanged()
            binding.isGenderCatory.setSelection(0)
            filterByGender(GenderType.ALL)
        }
    }

    private fun isGetStandardSection() {
        Log.d("isAcademicYearId", isAcademicYearId.toString())
        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            currentFilteredList
        } else {
            val searchWords = text.trim().lowercase().split("\\s+".toRegex())
            currentFilteredList.filter { student ->
                val fieldsToSearch = listOf(
                    student.name.lowercase(),
                    student.gender.lowercase(),
                    student.class_teacher.lowercase(),
                    student.roll_no.lowercase(),
                    student.admission_no.lowercase(),
                    student.class_name.lowercase(),
                    student.section_name.lowercase(),
                    student.dob.lowercase(),
                    student.father_name.lowercase(),
                    student.admission_no.lowercase(),
                    student.email.lowercase(),
                    student.primary_mobile.lowercase()
                )
                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            studentreportadapter.updateData(filteredList)
        } else {
            ErrorMessage(Constant.NO_DATA_FOUND)
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
                    updateStandardAndSection(selectedOption)
                    isSection = selectedOption.sections
                    hasUserSelectedSection = false
                    isLoadSection(isSection)
                    isAllSectionSelected = true
                    binding.isSpinnerSection.setSelection(0)
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard[position].id}, Year = ${isStandard[position].name}"
                    )
                    if (hasUserSelectedStandard) {
                        isGetStudentReport()
                    } else {
                        hasUserSelectedStandard = true
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }



    override fun onCamerClick(data: StudentReportData) {
        selectedStudentForProfileUpdate = data
        showBottomDialog()
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

        rlaDocument.visibility = View.GONE
        rlaVoice.visibility = View.GONE
        rlaVideoPick.visibility = View.GONE

        rlaGallery.setOnClickListener {
            singleImageLauncher?.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
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
            if (cameraPermissionDeniedCount >= 2 &&
                !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                showCameraPermissionSettingsDialog()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
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
            .setNegativeButton(getString(R.string.Cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
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
            Toast.makeText(this, getString(R.string.could_not_create_file_for_photo), Toast.LENGTH_SHORT).show()
            return
        }

        val photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraImageFilePath = photoFile.absolutePath

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.camera_not_available_on_this_device), Toast.LENGTH_SHORT).show()
            Log.e("CameraError", "Camera launch failed", e)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("${Constant.IMG_}${timeStamp}_", Constant.jpg, storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        if (requestCode == CAMERA_IMAGE_REQUEST) {
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
                    onProfileImageSelected(Uri.fromFile(file))
                } else {
                    Toast.makeText(this, getString(R.string.camera_image_file_not_found), Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, getString(R.string.camera_image_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProfileImageSelected(uri: Uri) {
        pendingProfileImageUri = uri
        showUpdateProfileConfirmationDialog()
    }

    private fun showUpdateProfileConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

        alertMessage.visibility = View.GONE
        lblSelectTarget.text = "Are you sure want to update the profile picture"

        okButton.setOnClickListener {
            alertDialog.dismiss()
            uploadProfileImageAndUpdate()
        }
        btnCancel.setOnClickListener {
            pendingProfileImageUri = null
            alertDialog.dismiss()
        }
    }

    private fun uploadProfileImageAndUpdate() {
        val student = selectedStudentForProfileUpdate
        val uri = pendingProfileImageUri
        if (student == null || uri == null) return

        Constant.showLoading(this)

        val outputDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
        outputDir.mkdirs()

        val fileItem = FileItem(uri.toString(), FileType.IMAGE)
        val isCountryId = SharedPreference.getCountryId(this)

        Constant.compressImageFilesOnly(
            context = this,
            files = listOf(fileItem),
            outputDir = outputDir.absolutePath,
            format = Bitmap.CompressFormat.JPEG,
            quality = 80,
            maxWidth = 1280,
            maxHeight = 1280,
            onEachProcessed = { _, outputPath, success ->
                if (success && outputPath != null) {
                    isAwsUploadingPreSigned?.getPreSignedUrl(
                        outputPath,
                        isStaffDetails?.school_id.toString(),
                        "profile_photo",
                        this,
                        isCountryId!!,
                        true,
                        object : UploadCallback {
                            override fun onUploadSuccess(response: String?, isFileUploaded: String?) {
                                File(outputPath).delete()
                                if (!isFileUploaded.isNullOrEmpty()) {
                                    callUpdateProfileApi(student.id.toString(), isFileUploaded)
                                } else {
                                    Constant.hideLoading(this@StudentReport)
                                    Toast.makeText(
                                        this@StudentReport,
                                        getString(R.string.Something_went_wrong_Please_try_again),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onUploadError(error: String?) {
                                File(outputPath).delete()
                                Constant.hideLoading(this@StudentReport)
                                Toast.makeText(
                                    this@StudentReport,
                                    error ?: getString(R.string.Something_went_wrong_Please_try_again),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                } else {
                    Constant.hideLoading(this@StudentReport)
                    Toast.makeText(
                        this@StudentReport,
                        getString(R.string.Something_went_wrong_Please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onComplete = { }
        )
    }

    private fun callUpdateProfileApi(studentId: String, filePath: String) {
        val jsonObject = JsonObject().apply {
            addProperty("file_path", filePath)
            addProperty("student_id", studentId)
        }
        appViewModel?.isupdateporiflestudent(isAccessToken!!, jsonObject, this)
    }


    fun ErrorMessage(ErrorMessage: String) {
        binding.rcyStudentReport.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }

    fun ShowData() {
        binding.rcyStudentReport.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    private fun updateStandardAndSection(standard: Standard?) {
        if (standard == null) {
            isClassID = null
            isSectionID = null
            binding.tabLayout.visibility = View.GONE
            ErrorMessage(resources.getString(R.string.no_standard_found))
            return
        }
        isClassID = standard.id
        isSection = standard.sections
        binding.tabLayout.visibility = View.VISIBLE

        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            // handled by spinner selection
        } else {
            isSectionID = null
            isSection = null
            binding.tabLayout.visibility = View.GONE
            ErrorMessage("${getString(R.string.No_Section_Found_in)} '${standard.name}'")
            return
        }
    }

    private fun highlightSelectedTab(selectedView: View) {
        binding.tapNoAsc.isEnabled = true
        binding.tapNoDsc.isEnabled = true
        binding.tapNameAsc.isEnabled = true
        binding.tapNameDsc.isEnabled = true
        binding.tapRollAsc.isEnabled = true
        binding.tapRollDsc.isEnabled = true

        binding.tapNoAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNoDsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameDsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapRollAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapRollDsc.setBackgroundResource(R.drawable.light_gray_radius)

        selectedView.setBackgroundResource(R.drawable.theme_colour_radius)
        selectedView.isEnabled = false
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgDelete -> {
                binding.rytSearchBar.visibility = View.GONE
                binding.txtSearchMenu.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)
            }

            R.id.tapRollAsc -> {
                highlightSelectedTab(binding.tapRollAsc)
                currentSortType = SortType.ROLL_ASC
                sortList(SortType.ROLL_ASC)
            }

            R.id.tapRollDsc -> {
                highlightSelectedTab(binding.tapRollDsc)
                currentSortType = SortType.ROLL_DESC
                sortList(SortType.ROLL_DESC)
            }

            R.id.tapNoAsc -> {
                highlightSelectedTab(binding.tapNoAsc)
                currentSortType = SortType.NO_ASC
                sortList(SortType.NO_ASC)
            }

            R.id.tapNoDsc -> {
                highlightSelectedTab(binding.tapNoDsc)
                currentSortType = SortType.NO_DESC
                sortList(SortType.NO_DESC)
            }

            R.id.tapNameAsc -> {
                highlightSelectedTab(binding.tapNameAsc)
                currentSortType = SortType.NAME_ASC
                sortList(SortType.NAME_ASC)
            }

            R.id.tapNameDsc -> {
                highlightSelectedTab(binding.tapNameDsc)
                currentSortType = SortType.NAME_DESC
                sortList(SortType.NAME_DESC)
            }
        }
    }

    private fun setupFilerCatoryTypeSpinner(forceTrigger: Boolean = false) {
        val adapter = SpinnerLoadingAdapter(this, filterCaterotyType)
        binding.isSpinnerSort.adapter = adapter
        adapter.selectedPosition = 0
        filterSelectedOption = filterCaterotyType[0]
        binding.isSpinnerSort.setSelection(0)
        adapter.notifyDataSetChanged()
        binding.isSpinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (adapter.selectedPosition != position || forceTrigger) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    filterSelectedOption = filterCaterotyType[position]
                    isGetStandardSection()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter

        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                binding.txtSearchMenu.text.clear()
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                val selectedOption = isAcademicYear!![position]
                Log.d("DropdownMenu", "Clicked Academic Year: ID = ${selectedOption.id}")
                isAcademicYearId = selectedOption.id
                if (hasAcademicYearManuallyChanged) {
                    setupFilerCatoryTypeSpinner(forceTrigger = true)
                } else {
                    hasAcademicYearManuallyChanged = true
                }
                binding.tabLayout.visibility = View.VISIBLE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupGenderCaterotyType(filterGenderCaterotyType: List<String>) {
        genderSpinnerAdapter = SpinnerLoadingAdapter(this, filterGenderCaterotyType)
        binding.isGenderCatory.adapter = genderSpinnerAdapter

        binding.isGenderCatory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                handleSpinnerSelection(position, genderSpinnerAdapter, filterGenderCaterotyType)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        genderSpinnerAdapter.selectedPosition = 0
        binding.isGenderCatory.setSelection(0)
        genderSpinnerAdapter.notifyDataSetChanged()
        handleSpinnerSelection(0, genderSpinnerAdapter, filterGenderCaterotyType)
    }

    private fun handleSpinnerSelection(
        position: Int, adapter: SpinnerLoadingAdapter, filterGenderCaterotyType: List<String>
    ) {
        if (adapter.selectedPosition != position) {
            binding.txtSearchMenu.text.clear()
            adapter.selectedPosition = position
            adapter.notifyDataSetChanged()
            filterSelectedOption = filterGenderCaterotyType[position]
            val genderType = when (filterSelectedOption) {
                getString(R.string.male) -> GenderType.MALE
                getString(R.string.female) -> GenderType.FEMALE
                getString(R.string.lblOthers) -> GenderType.OTHERS
                else -> GenderType.ALL
            }
            filterByGender(genderType)
        }
    }

    override fun onMailClick(data: StudentReportData) {
        Constant.redirectToMail(this, data.email, "", "")
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.primary_mobile)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessageOnly(this, data.primary_mobile)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraIntent()
            } else {
                cameraPermissionDeniedCount++
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_is_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isLoadSection(isSection: List<Section>?) {
        val updatedSections = mutableListOf<Section>().apply {
            add(Section(id = -1, name = resources.getString(R.string.all)))
            if (isSection != null) addAll(isSection)
        }

        val adapter = SectionDropDownListAdapter(this, updatedSections)
        binding.isSpinnerSection.adapter = adapter

        binding.isSpinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                val selectedOption = updatedSections[position]

                Log.d(
                    "DropdownMenu",
                    "Clicked Section: ID = ${selectedOption.id}, Name = ${selectedOption.name}"
                )

                if (hasUserSelectedSection) {
                    isSectionId = selectedOption.id
                    isSectionID = selectedOption.id
                    isAllSectionSelected = (position == 0)
                    isGetStudentReport()
                } else {
                    hasUserSelectedSection = true
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.isSpinnerSection.setSelection(0)
    }
}