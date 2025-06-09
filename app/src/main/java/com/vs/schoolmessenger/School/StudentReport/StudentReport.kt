package com.vs.schoolmessenger.School.StudentReport

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
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
    private var filterSelectedOption: String? = null
    private var hasUserSelectedSection = false
    private var hasUserSelectedStandard = false


    val filterCaterotyType = listOf(
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
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            binding.rytSearchBar.visibility = View.VISIBLE
        }
        binding.imgDelete.setOnClickListener(this)
        binding.tapNameAsc.setOnClickListener(this)
        binding.tapNoDsc.setOnClickListener(this)
        binding.tapNameDsc.setOnClickListener(this)
        binding.tapNoAsc.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
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
                        isLoadAcademicYear(isAcademicYear)
                        isValidAcademicYear =
                            isAcademicYear?.any { it.current_academic_year == true } == true
                        isAcademicYearId = isAcademicYear!![0].id
                        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                        Log.d("isAcademicYearId", isAcademicYearId.toString())
                        isGetStandardSection()
                    }
                } else {
                    binding.tabLayout.visibility = View.GONE
//                    binding.rlaStandardPicking.visibility = View.GONE
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
                    binding.tabLayout.visibility = View.GONE
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
                        binding.rlaStandardPicking.visibility = View.VISIBLE
                        isClassID = isGetStandard!!.get(0).id
                        isSectionID = isGetStandard!!.get(0).sections.get(0).id
                        isLoadStandard(isGetStandard)
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            isSection = isGetStandard!!.get(0).sections
                        }
                        isGetStudentReport()
                    } else {
                        binding.tabLayout.visibility = View.GONE
//                        binding.rlaStandardPicking.visibility=View.GONE
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
                filter(s.toString())

            }
        })
    }

    private fun isGetStudentReport() {
        binding.txtSearchMenu.text.clear()
        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter


        if (Constant.GET_ALL_STUDENT == filterSelectedOption) {
            binding.lnrStandardDetails.visibility = View.GONE
            binding.lnrSectionDetails.visibility = View.GONE
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!,isAcademicYearId = isAcademicYearId, activity = this
            )
        }

        if (Constant.STANDARD == filterSelectedOption) {
            binding.lnrStandardDetails.visibility = View.VISIBLE
            binding.lnrSectionDetails.visibility = View.GONE
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!,isAcademicYearId = isAcademicYearId, class_id = isClassID!!, activity = this
            )
        }
        if (Constant.STANDARD_AND_SECTION == filterSelectedOption) {
            binding.lnrStandardDetails.visibility = View.VISIBLE
            binding.lnrSectionDetails.visibility = View.VISIBLE
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!,isAcademicYearId = isAcademicYearId, class_id = isClassID!!, section_id = isSectionID!!, activity = this
            )
        }
    }

    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
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

                    if (hasUserSelectedSection) {
                        isSectionId = selectedOption.id
                        isSectionID = selectedOption.id
                        isGetStudentReport()

                    } else {
                        // First auto-trigger — just set the flag and skip loadData
                        hasUserSelectedSection = true
                    }

                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
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

//            binding.dropdownTextViewStandard.text = "-"
//            binding.dropdownTextViewSection.text = "-"
            binding.tabLayout.visibility = View.GONE
//            binding.dropdownTextViewSection.isEnabled = false
//            binding.dropdownTextViewSection.isClickable = false

            ErrorMessage(Constant.No_STANDARD_FOUND)
            return
        }
        // Set selected Standard
        isClassID = standard.id
        isSection = standard.sections
//        binding.dropdownTextViewStandard.text = standard.name
        binding.tabLayout.visibility = View.VISIBLE

        val sections = standard.sections
        if (!sections.isNullOrEmpty()) {
            val defaultSection = sections[0]
            isSectionID = defaultSection.id
//            binding.dropdownTextViewSection.text = defaultSection.name

            if (sections.size == 1) {
//                // Only one section -> disable dropdown
//                binding.dropdownTextViewSection.isEnabled = false
//                binding.dropdownTextViewSection.isClickable = false
            } else {
                // Multiple sections -> enable dropdown
//                binding.dropdownTextViewSection.isEnabled = true
//                binding.dropdownTextViewSection.isClickable = true
            }
        } else {
            // No sections -> reset and disable section dropdown
            isSectionID = null
            isSection = null
//            binding.dropdownTextViewSection.text = "-"
            binding.tabLayout.visibility = View.GONE
//            binding.dropdownTextViewSection.isEnabled = false
//            binding.dropdownTextViewSection.isClickable = false
            ErrorMessage("No Section Found in '${standard.name}'")
            return
        }
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


            R.id.imgDelete -> {
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

    private fun setupFilerCatoryTypeSpinner(forceTrigger: Boolean = false) {
        val adapter = SpinnerLoadingAdapter(this, filterCaterotyType)
        binding.isSpinnerSort.adapter = adapter
        // Preselect first item
        adapter.selectedPosition = 0
        filterSelectedOption = filterCaterotyType[0]
        binding.isSpinnerSort.setSelection(0)
        adapter.notifyDataSetChanged()

        binding.isSpinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (adapter.selectedPosition != position || forceTrigger) {
                        adapter.selectedPosition = position
                        adapter.notifyDataSetChanged()
                        filterSelectedOption = filterCaterotyType[position]
                        isGetStandardSection() // Only triggered with valid selection
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter

        binding.isSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    val selectedOption = isAcademicYear!![position]
                    Log.d("DropdownMenu", "Clicked Academic Year: ID = ${selectedOption.id}")
                    isAcademicYearId = selectedOption.id

                    // Reset and trigger 0th item in category
                    setupFilerCatoryTypeSpinner(forceTrigger = true)

                    binding.tabLayout.visibility = View.VISIBLE
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    override fun onMailClick(data: StudentReportData) {
        Constant.redirectToMail(this, data.email, "", "")
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.primary_mobile)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}


