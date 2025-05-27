package com.vs.schoolmessenger.School.StudentReport

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentReportBinding

class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
    StudentReportClickListener {
    private var appViewModel: App? = null
    private lateinit var mAdapter: StudentReportAdapter
    private lateinit var isStudentReportData: List<StudentReportData>
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

    val items = listOf(
        Constant.GET_ALL_STUDENT,
        Constant.STANDARD,
        Constant.STANDARD_AND_SECTION
    )


    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)
        binding.AcademicYear.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            binding.rytSearchBar.visibility = View.VISIBLE
        }
        binding.imgDelete.setOnClickListener(this)
        binding.tapNameAsc.setOnClickListener(this)
        binding.tapNoDsc.setOnClickListener(this)
        binding.tapNameDsc.setOnClickListener(this)
        binding.tapNoAsc.setOnClickListener(this)
        binding.dropdownTextViewStandard.setOnClickListener(this)
        binding.dropdownTextViewSection.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        //Setting the first "Get All Student" as Default text in the Sort Option
        binding.dropdownTextView.text = items[0]
        isAccessToken = isStaffDetails!!.access_token
        Log.d("isAccessToken", isStaffDetails!!.access_token)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.StudentReport)
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        isGetAcademicYear()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    response.data.let { academicList ->
                        Log.d("AcademicYearResponse", response.data.toString())
                        val reorderedList =
                            academicList.sortedByDescending { it.current_academic_year }
                        if (isAcademicYear == reorderedList) return@observe
                        isAcademicYear = reorderedList
                        isValidAcademicYear =
                            isAcademicYear?.any { it.current_academic_year == true } == true
                        binding.lblAcademicYear.text = isAcademicYear!![0].year
                        isAcademicYearId = isAcademicYear!![0].id
                        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                        Log.d("isAcademicYearId", isAcademicYearId.toString())
                        isGetStandardSection()
                    }
                } else {
                    binding.tabLayout.visibility = View.GONE
                    binding.rlaStandardPicking.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            }
        }

        appViewModel!!.isStudentReportList?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    ShowData()
                    val isStudentReportResponseData = response.data
                    isStudentReportData = isStudentReportResponseData
                    loadStudentReport(isStudentReportData)
                } else {
                    binding.tabLayout.visibility=View.GONE
                    ErrorMessage(response.message)
                }
            }

        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        binding.rlaStandardPicking.visibility=View.VISIBLE
                        isClassID = isGetStandard!!.get(0).id
                        Log.d("isClassID", isClassID.toString())
//                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                        isSectionID = isGetStandard!!.get(0).sections.get(0).id

                        binding.dropdownTextViewStandard.text = isGetStandard!!.get(0).name
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            binding.dropdownTextViewSection.text =
                                isGetStandard!!.get(0).sections.get(0).name
                            isSection = isGetStandard!!.get(0).sections
                            Log.d("isSectionID", isSection.toString())
                        }
                        isGetStudentReport()
                    }
                    else {
                        binding.tabLayout.visibility=View.GONE
                        binding.rlaStandardPicking.visibility=View.GONE
                        ErrorMessage(response.message)
                    }
                }
            }
        }


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextSSS",s.toString())
                filter(s.toString())

            }
        })
    }
    private fun isGetStudentReport() {
        binding.txtSearchMenu.text.clear()
        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter

        Log.d("SectionID", isSectionID.toString())
        Log.d("ClassID", isClassID.toString())

        if (Constant.GET_ALL_STUDENT == binding.dropdownTextView.text) {
            binding.lnrStandardDetails.visibility = View.GONE
            binding.lnrSectionDetails.visibility = View.GONE
            Log.d("GET_ALL_STUDENT", "Get all student")
            Log.d("SectionID", isSectionID.toString())
            Log.d("ClassID", isClassID.toString())
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!, activity = this
            )
        }

        if (Constant.STANDARD == binding.dropdownTextView.text) {
            binding.lnrStandardDetails.visibility = View.VISIBLE
            binding.lnrSectionDetails.visibility = View.GONE
            isGetAcademicYear()
            Log.d("STANDARD", "STANDARD")
            Log.d("ClassID", isClassID.toString())
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!, class_id = isClassID!!, activity = this
            )
        }
        if (Constant.STANDARD_AND_SECTION == binding.dropdownTextView.text) {
            binding.lnrStandardDetails.visibility = View.VISIBLE
            binding.lnrSectionDetails.visibility = View.VISIBLE
            isGetAcademicYear()
            Log.d("STANDARD&SECTION", "STANDARD&SECTION")
            Log.d("SectionID", isSectionID.toString())
            Log.d("ClassID", isClassID.toString())

            appViewModel!!.getStudentReportDetails(
                isAccessToken!!, class_id = isClassID!!, section_id = isSectionID!!, activity = this
            )
        }
    }

    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
        // Once data is loaded, stop shimmer and pass the actual data
        mAdapter =
            StudentReportAdapter(studentReportData, this, this, Constant.isShimmerViewDisable)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        // Set GridLayoutManager (2 columns in this case)
        binding.rcyStudentReport.adapter = mAdapter
        highlightSelectedTab(binding.tapNameAsc)
        mAdapter.sortData(StudentReportAdapter.SortType.NAME_ASC)
    }

    private fun isGetAcademicYear() {
        Log.d("isGetAcademicYear", "Getting")
        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    private fun isGetStandardSection() {
        Log.d("isAcademicYearId", isAcademicYearId.toString())
        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            isStudentReportData
        } else {

            val searchWords = text.trim().lowercase().split("\\s+".toRegex())

            isStudentReportData.filter { student ->
                val fieldsToSearch = listOf(
                    student.name.lowercase(),
                    student.admission_no.lowercase(),
                    student.email.lowercase(),
                    student.primary_mobile.lowercase()
                )

                // Check if ALL search words are found in ANY of the fields(feildTosearch List i.e name,email...etc)
                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }

            }
        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            mAdapter.updateData(filteredList)
        } else {
            ErrorMessage(Constant.NO_DATA_FOUND)
        }
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
            // No Standard Found
            isClassID = null
            isSectionID = null

            binding.dropdownTextViewStandard.text = "-"
            binding.dropdownTextViewSection.text = "-"
            binding.tabLayout.visibility = View.GONE
            binding.dropdownTextViewSection.isEnabled = false
            binding.dropdownTextViewSection.isClickable = false

            ErrorMessage(Constant.No_STANDARD_FOUND)
            return
        }

        // Set selected Standard
        isClassID = standard.id
        isSection = standard.sections
        binding.dropdownTextViewStandard.text = standard.name
        binding.tabLayout.visibility = View.VISIBLE

        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            isSectionID = defaultSection.id
            binding.dropdownTextViewSection.text = defaultSection.name

            if (sections.size == 1) {
                // Only one section -> disable dropdown
                binding.dropdownTextViewSection.isEnabled = false
                binding.dropdownTextViewSection.isClickable = false
            } else {
                // Multiple sections -> enable dropdown
                binding.dropdownTextViewSection.isEnabled = true
                binding.dropdownTextViewSection.isClickable = true
            }

        } else {
            // No sections -> reset and disable section dropdown
            isSectionID = null
            isSection = null
            binding.dropdownTextViewSection.text = "-"
            binding.tabLayout.visibility = View.GONE
            binding.dropdownTextViewSection.isEnabled = false
            binding.dropdownTextViewSection.isClickable = false

            ErrorMessage("No Section Found in '${standard.name}'")
            return
        }

        // Safe to call API now
        isGetStudentReport()
    }

    private fun highlightSelectedTab(selectedView: View) {
        // Reset all tabs to white
        binding.tapNoAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNoDsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameDsc.setBackgroundResource(R.drawable.light_gray_radius)
        // Highlight the selected tab
        selectedView.setBackgroundResource(R.drawable.theme_colour_radius)
    }
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

//            R.id.rlaSort -> {
//                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
//                    binding.dropdownTextView.text = selectedOption
//                    Log.d("SelectedFilterSort", selectedOption)
//
//                    if (!isGetStandard.isNullOrEmpty()) {
//                        updateStandardAndSection(isGetStandard!![0])
//                    } else {
//                        updateStandardAndSection(null)
//                    }
//                }
//            }


            R.id.dropdownTextViewStandard -> {
                showStandardDropdown(binding.dropdownTextViewStandard, this, isGetStandard) { selectStandard, _ ->
                    updateStandardAndSection(selectStandard)
                }
            }


            R.id.dropdownTextViewSection -> {
                if (!isSection.isNullOrEmpty() && binding.dropdownTextViewSection.isEnabled) {
                    isDropDownLoadDataSection(
                        binding.dropdownTextViewSection,
                        this,
                        isSection
                    ) { selectedOption ->
                        binding.dropdownTextViewSection.text = selectedOption.first
                        isSectionID = selectedOption.second
                        isGetStudentReport()
                    }
                }
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
                    //Every times when i change the AcademicYear i should get the default GET ALL STUDENT Data
                    binding.dropdownTextView.text=Constant.GET_ALL_STUDENT
                    isGetStudentReport()
                    binding.tabLayout.visibility=View.VISIBLE
                }
            }
            R.id.imgDelete ->{
                binding.rytSearchBar.visibility = View.GONE
            }

            R.id.tapNoAsc -> {
                highlightSelectedTab(binding.tapNoAsc)
                mAdapter.sortData(StudentReportAdapter.SortType.NO_ASC)
            }

            R.id.tapNoDsc -> {
                highlightSelectedTab(binding.tapNoDsc)
                mAdapter.sortData(StudentReportAdapter.SortType.NO_DESC)
            }

            R.id.tapNameAsc -> {
                highlightSelectedTab(binding.tapNameAsc)
                mAdapter.sortData(StudentReportAdapter.SortType.NAME_ASC)
            }

            R.id.tapNameDsc -> {
                highlightSelectedTab(binding.tapNameDsc)
                mAdapter.sortData(StudentReportAdapter.SortType.NAME_DESC)
            }
        }

    }
    override fun onMailClick(data: StudentReportData) {
        Constant.redirectToMail(this, data.email,"","")
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.primary_mobile)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}

