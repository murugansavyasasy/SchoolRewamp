package com.vs.schoolmessenger.School.StudentReport

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
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
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.StudentReportBinding

class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
    StudentReportClickListener {
    private var appViewModel: App? = null
    private lateinit var mAdapter: StudentReportAdapter
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
    private var currentSortType: SortType = SortType.NO_ASC
    private lateinit var genderSpinnerAdapter: SpinnerLoadingAdapter


    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener{
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
        // Prevent immediate onItemSelected from re-triggering data load
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
                    mAdapter.updateData(emptyList())
                    binding.tabLayout.visibility = View.GONE
                    ErrorMessage(response.message)
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                    binding.rytSearchBar.visibility = View.GONE
                }
            }else{
                originalStudentList = emptyList()
                currentFilteredList = emptyList()
                mAdapter.updateData(emptyList())
                binding.tabLayout.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
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
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                        binding.rytSearchBar.visibility = View.GONE
                        isGetStudentReport()
                    } else {
                        originalStudentList = emptyList()
                        currentFilteredList = emptyList()
                        mAdapter.updateData(emptyList())
                        binding.tabLayout.visibility = View.GONE
                        binding.rlaStandardPicking.visibility = View.GONE
                        ErrorMessage(response.message)
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                        binding.rytSearchBar.visibility = View.GONE
                    }
                }
            }
            else{
                originalStudentList = emptyList()
                currentFilteredList = emptyList()
                mAdapter.updateData(emptyList())
                binding.tabLayout.visibility = View.GONE
                binding.rlaStandardPicking.visibility = View.GONE
                ErrorMessage(getString(R.string.something_went_wrong_please_try_again_later))
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                binding.rytSearchBar.visibility = View.GONE

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
            SortType.ROLL_ASC -> currentFilteredList.sortedBy { it.roll_no }
            SortType.ROLL_DESC -> currentFilteredList.sortedByDescending { it.roll_no }
            SortType.NO_ASC -> currentFilteredList.sortedBy { it.admission_no }
            SortType.NO_DESC -> currentFilteredList.sortedByDescending { it.admission_no }
            SortType.NAME_ASC -> currentFilteredList.sortedBy { it.name }
            SortType.NAME_DESC -> currentFilteredList.sortedByDescending { it.name }
        }
        mAdapter.updateData(sortedList)

        if (binding.txtSearchMenu.text.isNotEmpty()){
            filter(binding.txtSearchMenu.text.toString())
        }else{
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
        }
//        mAdapter.updateData(currentFilteredList)
        sortList(currentSortType)

    }

    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
        if(studentReportData.isNullOrEmpty()){
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytSearchBar.visibility = View.GONE
        }
        else{
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rytSearchBar.visibility = View.GONE
            originalStudentList = studentReportData
            currentFilteredList = originalStudentList

            mAdapter =
                StudentReportAdapter(currentFilteredList, this, this, Constant.isShimmerViewDisable)
            binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
            binding.rcyStudentReport.adapter = mAdapter
            //whenever we call the student report we make it as default gender filter all and sort NoAsc
            genderSpinnerAdapter.selectedPosition = 0
            genderSpinnerAdapter.notifyDataSetChanged()
            binding.isGenderCatory.setSelection(0)
            filterByGender(GenderType.ALL)
            highlightSelectedTab(binding.tapNameAsc)
            sortList(SortType.NAME_ASC)
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
        // make all tabs to  clickable
        binding.tapNoAsc.isEnabled = true
        binding.tapNoDsc.isEnabled = true
        binding.tapNameAsc.isEnabled = true
        binding.tapNameDsc.isEnabled = true
        binding.tapRollAsc.isEnabled = true
        binding.tapRollDsc.isEnabled = true
        // Reset all tabs to white
        binding.tapNoAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNoDsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapNameDsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapRollAsc.setBackgroundResource(R.drawable.light_gray_radius)
        binding.tapRollDsc.setBackgroundResource(R.drawable.light_gray_radius)

        // Highlight the selected tab
        selectedView.setBackgroundResource(R.drawable.theme_colour_radius)
        // make the selected tab as not clickable
        selectedView.isEnabled = false
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.imgDelete -> {
                binding.rytSearchBar.visibility = View.GONE

                binding.txtSearchMenu.setText("")

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)
            }

            R.id.tapRollAsc -> {
                highlightSelectedTab(binding.tapRollAsc)
                sortList(SortType.ROLL_ASC)
            }

            R.id.tapRollDsc -> {
                highlightSelectedTab(binding.tapRollDsc)
                sortList(SortType.ROLL_DESC)
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
        binding.isSpinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
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

        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                binding.txtSearchMenu.text.clear()
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
        genderSpinnerAdapter = SpinnerLoadingAdapter(this, filterGenderCaterotyType)
        binding.isGenderCatory.adapter = genderSpinnerAdapter

        binding.isGenderCatory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    handleSpinnerSelection(position, genderSpinnerAdapter, filterGenderCaterotyType)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        // Preselect first item manually
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
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}