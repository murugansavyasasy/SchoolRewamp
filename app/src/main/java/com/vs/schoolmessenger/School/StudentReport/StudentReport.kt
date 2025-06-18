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
    var isAllSectionSelected = true
    private var hasAcademicYearManuallyChanged = false
    private lateinit var filterCaterotyType: List<String>
    private var originalStudentList: List<StudentReportData> = listOf()
    private var currentFilteredList: List<StudentReportData> = listOf()

    enum class SortType { NO_ASC, NO_DESC, NAME_ASC, NAME_DESC }
    enum class GenderType { ALL, MALE, FEMALE, OTHERS }
    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        filterCaterotyType = listOf(
            resources.getString(R.string.get_all_student),
            resources.getString(R.string.standard_and_section)
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
        setupGenderCaterotyType(filterGenderCaterotyType)


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
                        // Prevent immediate onItemSelected from re-triggering data load
                        hasAcademicYearManuallyChanged = false
                        if (filterSelectedOption != null) {
                            isGetStandardSection()
                        }
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
                        isAllSectionSelected = true
                        isLoadStandard(isGetStandard)
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            isSection = isGetStandard!!.get(0).sections
                        }
                        isGetStudentReport()
                    } else {
                        binding.tabLayout.visibility = View.GONE
                        binding.rlaStandardPicking.visibility = View.GONE
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
                Log.d("TextSSS", s.toString())
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
            binding.rlaStandardPicking.visibility = View.GONE
            appViewModel!!.getStudentReportDetails(
                isAccessToken!!, isAcademicYearId, activity = this
            )
        }

        if (Constant.STANDARD_AND_SECTION == filterSelectedOption) {
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
        val sortedList = when (sortType) {
            SortType.NO_ASC -> currentFilteredList.sortedBy { it.admission_no }
            SortType.NO_DESC -> currentFilteredList.sortedByDescending { it.admission_no }
            SortType.NAME_ASC -> currentFilteredList.sortedBy { it.name }
            SortType.NAME_DESC -> currentFilteredList.sortedByDescending { it.name }
        }
        mAdapter.updateData(sortedList)
    }


    private fun filterByGender(genderType: GenderType) {
        currentFilteredList = when (genderType) {
            GenderType.ALL -> originalStudentList.sortedBy { it.gender }
            GenderType.MALE -> originalStudentList.filter { it.gender.equals("Male", true) }
            GenderType.FEMALE -> originalStudentList.filter { it.gender.equals("Female", true) }
            GenderType.OTHERS -> originalStudentList.filter { it.gender.equals("Others", true) }
        }

        Log.d("filteredGenderList", currentFilteredList.size.toString())

        if (currentFilteredList.isEmpty()) {
            ErrorMessage(getString(R.string.no_student_found))

        } else {
            ShowData()

        }

        mAdapter.updateData(currentFilteredList)
    }


    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
        originalStudentList = studentReportData
        currentFilteredList = originalStudentList

        mAdapter =
            StudentReportAdapter(currentFilteredList, this, this, Constant.isShimmerViewDisable)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter
        highlightSelectedTab(binding.tapNameAsc)
        sortList(SortType.NO_ASC)
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
                    // Select "All" section by default
                    isAllSectionSelected = true
                    binding.isSpinnerSection.setSelection(0)


                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard[position].id}, Year = ${isStandard[position].name}"
                    )
                    //new
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
        // Inserting "All" section manually in the <Section> List
        val updatedSections = mutableListOf<Section>().apply {
            add(
                Section(
                    id = -1,// ID -1 just as a placeholder, logic based on position
                    name = resources.getString(R.string.all)
                )
            )
            if (isSection != null) addAll(isSection)
        }

        val adapter = SectionDropDownListAdapter(this, updatedSections)
        binding.isSpinnerSection.adapter = adapter

        binding.isSpinnerSection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
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
        // Force default selection to "All"
        binding.isSpinnerSection.setSelection(0)
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
            binding.tabLayout.visibility = View.GONE
            ErrorMessage(resources.getString(R.string.no_standard_found))
            return
        }
        // Set selected Standard
        isClassID = standard.id
        isSection = standard.sections
        binding.tabLayout.visibility = View.VISIBLE

        val sections = standard.sections
        //Requirement changed to ("All") before we select default section as 0 index of section list from api
        if (!sections.isNullOrEmpty()) {
            //now the requirement has been changed if we want to handle the section,if it comes null we can do here!
            if (sections.size == 1) {
                // Only one section -> disable Spinner keep that section(one section) and disable the spinner
            } else {
                // Multiple sections -> enable spinner
            }
        } else {
            // No sections -> reset and disable section spinner
            isSectionID = null
            isSection = null
            binding.tabLayout.visibility = View.GONE
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
                sortList(SortType.NO_ASC)
            }

            R.id.tapNoDsc -> {
                highlightSelectedTab(binding.tapNoDsc)
                sortList(SortType.NO_DESC)
            }

            R.id.tapNameAsc -> {
                highlightSelectedTab(binding.tapNameAsc)
                sortList(SortType.NAME_ASC)
            }

            R.id.tapNameDsc -> {
                highlightSelectedTab(binding.tapNameDsc)
                sortList(SortType.NAME_DESC)
            }
        }
    }


    private fun setupFilerCatoryTypeSpinner(forceTrigger: Boolean = false) {
        val adapter = SpinnerLoadingAdapter(this, filterCaterotyType)
        binding.isSpinnerSort.adapter = adapter
        adapter.selectedPosition = 0 // Preselect first item
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
                    if (hasAcademicYearManuallyChanged) {
                        // Reset and trigger 0th item in category
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
        val adapter = SpinnerLoadingAdapter(this, filterGenderCaterotyType)
        binding.isGenderCatory.adapter = adapter

        binding.isGenderCatory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    handleSpinnerSelection(position, adapter, filterGenderCaterotyType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        // Preselect first item manually
        adapter.selectedPosition = 0
        binding.isGenderCatory.setSelection(0)
        adapter.notifyDataSetChanged()
        handleSpinnerSelection(0, adapter, filterGenderCaterotyType)
    }

    private fun handleSpinnerSelection(
        position: Int,
        adapter: SpinnerLoadingAdapter,
        filterGenderCaterotyType: List<String>
    ) {
        if (adapter.selectedPosition != position) {
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
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}