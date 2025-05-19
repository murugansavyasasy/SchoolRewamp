package com.vs.schoolmessenger.School.StudentReport

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.compose.ui.platform.LocalDensity
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

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)
        binding.AcademicYear.setOnClickListener(this)
        binding.SearchNotification.setOnClickListener(this)
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
        isGetAcademicYear()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
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
                    ErrorMessage(response.message)
                }
            }
        }

        appViewModel!!.isStudentReportList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                if (response.status) {
                    ShowData()
                    Log.d("isStudentReportResponsestatus", response.status.toString())
                    Log.d("isStudentReportResponseMessage", response.message)
                    Log.d("isStudentReportList", response.data.toString())
                    Log.d("isStudentReportListSize", response.data.size.toString())
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
//            Constant.hideLoading(this@StudentReport)
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
//                        binding.dropdownTextViewStandard.visibility = View.GONE
//                        binding.dropdownTextViewSection.visibility = View.GONE
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
//        //Called at initial because we load default Get ALL Student first
//        Log.d("InitialCall","InitialGetAllStudentData")
//        appViewModel!!.getStudentReportDetails(
//            isAccessToken!!, activity = this
//        )

//        appViewModel!!.getStudentReportDetails(
//            isAccessToken!!, isClassID!!, isSectionID!!, this
//        )
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
//        Constant.showLoading(this@StudentReport)

        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    private fun isGetStandardSection() {
        Log.d("isAcademicYearId", isAcademicYearId.toString())
//        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun filter(text: String) {

        val filteredList = if (text.isEmpty()) {
            isStudentReportData
        } else {
            isStudentReportData.filter { it.name.contains(text, ignoreCase = true) }
        }

        if(filteredList.size>1){
            ShowData()
            (mAdapter).updateData(filteredList)
        }
        else{
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
        binding.tapNoAsc.setBackgroundResource(R.drawable.rect_light_gray)
        binding.tapNoDsc.setBackgroundResource(R.drawable.rect_light_gray)
        binding.tapNameAsc.setBackgroundResource(R.drawable.rect_light_gray)
        binding.tapNameDsc.setBackgroundResource(R.drawable.rect_light_gray)
        // Highlight the selected tab
        selectedView.setBackgroundResource(R.drawable.white_radious)
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaSort -> {
                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
                    binding.dropdownTextView.text = selectedOption
                    Log.d("SelectedFilterSort", selectedOption)

                    if (!isGetStandard.isNullOrEmpty()) {
                        updateStandardAndSection(isGetStandard!![0])
                    } else {
                        updateStandardAndSection(null)
                    }
                }
            }


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
            R.id.SearchNotification ->{
                binding.rytSearch.visibility=View.VISIBLE
            }
            R.id.imgDelete ->{
                binding.rytSearch.visibility=View.GONE
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






//We no longer need this animation because out of requiremnet
//            R.id.rlaStandard -> {
//                if (isVisibilityStandard) {
//                    isVisibilityStandard = false
//                    val animation = AnimationUtils.loadAnimation(
//                        applicationContext,
//                        R.anim.top_to_bottom_animation
//                    )
//                    binding.rlaStandardPicking.startAnimation(animation)
//                    binding.rlaStandardPicking.visibility = View.VISIBLE
//
//                } else {
//                    isVisibilityStandard = true
//                    val animation =
//                        AnimationUtils.loadAnimation(applicationContext, R.anim.slide_animation)
//                    binding.rlaStandardPicking.startAnimation(animation)
//
//                    handler.postDelayed({
//                        binding.rlaStandardPicking.visibility = View.GONE
//                    }, 400)
//                }
//            }
        }
    }

    override fun onMailClick(data: StudentReportData) {
//        In Get Student Report API,We have not recived the Email-->10/05/2025
        Constant.redirectToMail(this, data.email,"","")
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.primary_mobile)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}


//existing santhosh
//package com.vs.schoolmessenger.School.StudentReport
//
//import android.os.Handler
//import android.os.Looper
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.View
//import android.view.animation.AnimationUtils
//import androidx.compose.ui.platform.LocalDensity
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
//import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
//import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
//import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.SharedPreference
//import com.vs.schoolmessenger.databinding.StudentReportBinding
//
//class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
//    StudentReportClickListener {
//    private var appViewModel: App? = null
//    private lateinit var mAdapter: StudentReportAdapter
//    private lateinit var isStudentReportData: List<StudentReportData>
//    private var isAccessToken: String? = null
//    var isSection: List<Section>? = null
//    var isValidAcademicYear = false
//    var isAcademicYear: List<AcademicYear>? = null
//    private var isStaffDetails: StaffDetails? = null
//    var isAcademicYearId = -1
//    var isCurrentAcademicYear = true
//    var isSectionId = -1
//    var isGetStandard: List<Standard>? = null
//    private var isClassID: Int? = null
//    private var isSectionID: Int? = null
//
//    val items = listOf(
//        Constant.GET_ALL_STUDENT,
//        Constant.STANDARD,
//        Constant.STANDARD_AND_SECTION
//    )
//
//
//    val itemsStandard = listOf(
//        "XII",
//        "XI",
//        "X",
//        "IX",
//        "VII"
//    )
//    val itemsSection = listOf(
//        "A",
//        "B",
//        "C",
//        "D",
//        "E"
//    )
//
//    val handler = Handler(Looper.getMainLooper())
//
//    override fun getViewBinding(): StudentReportBinding {
//        return StudentReportBinding.inflate(layoutInflater)
//    }
//
//    private var isVisibilityStandard = true
//
//    override fun setupViews() {
//        super.setupViews()
//        setupToolbar()
//
//        appViewModel = ViewModelProvider(this)[App::class.java]
//        appViewModel!!.init()
//
//        binding.imgBack.setOnClickListener(this)
//        binding.rlaSort.setOnClickListener(this)
//        binding.AcademicYear.setOnClickListener(this)
//
////        binding.rlaStandard.setOnClickListener(this)
//        binding.dropdownTextViewStandard.setOnClickListener(this)
//        binding.dropdownTextViewSection.setOnClickListener(this)
//        isStaffDetails = SharedPreference.getStaffDetails(this)
//        //Setting the first "Get All Student" as Default text in the Sort Option
//        binding.dropdownTextView.text = items[0]
//        isAccessToken = isStaffDetails!!.access_token
//        Log.d("isAccessToken", isStaffDetails!!.access_token)
//        isGetAcademicYear()
////        isGetStudentReport()
//
//        appViewModel!!.isGetAcademicList?.observe(this) { response ->
////            Constant.hideLoading(this@StudentReport)
//            if (response != null) {
//                if (response.status) {
//                    response?.data?.let { academicList ->
//                        Log.d("AcademicYearResponse", response?.data.toString())
//                        val reorderedList =
//                            academicList.sortedByDescending { it.current_academic_year }
//                        if (isAcademicYear == reorderedList) return@observe
//                        isAcademicYear = reorderedList
//                        isValidAcademicYear =
//                            isAcademicYear?.any { it.current_academic_year == true } == true
//                        binding.lblAcademicYear.text = isAcademicYear!![0].year
//                        isAcademicYearId = isAcademicYear!![0].id
//                        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
//                        Log.d("isAcademicYearId", isAcademicYearId.toString())
//                        isGetStandardSection()
//                    }
//                } else {
//                    ErrorMessage(response.message)
//                }
//            }
//        }
//
//        appViewModel!!.isStudentReportList?.observe(this) { response ->
////            Constant.hideLoading(this@StudentReport)
//            if (response != null) {
//                if (response.status) {
//                    ShowData()
//                    Log.d("isStudentReportResponsestatus", response.status.toString())
//                    Log.d("isStudentReportResponseMessage", response.message)
//                    Log.d("isStudentReportList", response.data.toString())
//                    Log.d("isStudentReportListSize", response.data.size.toString())
//                    val isStudentReportResponseData = response.data
//                    isStudentReportData = isStudentReportResponseData
//                    loadStudentReport(isStudentReportData)
//                } else {
//                    ErrorMessage(response.message)
//                }
//            }
//
//        }
//
//        appViewModel!!.isStandardSectionList?.observe(this) { response ->
////            Constant.hideLoading(this@StudentReport)
//            if (response != null) {
//                isGetStandard = response.data
//                isGetStandard?.size?.let {
//                    if (it > 0) {
//                        isClassID = isGetStandard!!.get(0).id
//                        Log.d("isClassID", isClassID.toString())
////                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
//                        isSectionID = isGetStandard!!.get(0).sections.get(0).id
//
//                        binding.dropdownTextViewStandard.text = isGetStandard!!.get(0).name
//                        if (isGetStandard!!.get(0).sections.size > 0) {
//                            binding.dropdownTextViewSection.text =
//                                isGetStandard!!.get(0).sections.get(0).name
//                            isSection = isGetStandard!!.get(0).sections
//                            Log.d("isSectionID", isSection.toString())
//                        }
//                        isGetStudentReport()
//                    } else {
//                        binding.dropdownTextViewStandard.visibility = View.GONE
//                        binding.dropdownTextViewSection.visibility = View.GONE
//                    }
//                }
//            }
//        }
//
//
//        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                filter(s.toString())
//            }
//        })
//    }
//
//
//    private fun isGetStudentReport() {
//        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
//        binding.rcyStudentReport.adapter = mAdapter
//
//        Log.d("SectionID", isSectionID.toString())
//        Log.d("ClassID", isClassID.toString())
//
//        if (Constant.GET_ALL_STUDENT == binding.dropdownTextView.text) {
//            binding.lnrStandardDetails.visibility = View.GONE
//            binding.lnrSectionDetails.visibility = View.GONE
//            Log.d("GET_ALL_STUDENT", "Get all student")
//            Log.d("SectionID", isSectionID.toString())
//            Log.d("ClassID", isClassID.toString())
//            appViewModel!!.getStudentReportDetails(
//                isAccessToken!!, activity = this
//            )
//        }
//
//        if (Constant.STANDARD == binding.dropdownTextView.text) {
//            binding.lnrStandardDetails.visibility = View.VISIBLE
//            binding.lnrSectionDetails.visibility = View.GONE
//            isGetAcademicYear()
//            Log.d("STANDARD", "STANDARD")
//            Log.d("ClassID", isClassID.toString())
//            appViewModel!!.getStudentReportDetails(
//                isAccessToken!!, class_id = isClassID!!, activity = this
//            )
//        }
//        if (Constant.STANDARD_AND_SECTION == binding.dropdownTextView.text) {
//            binding.lnrStandardDetails.visibility = View.VISIBLE
//            binding.lnrSectionDetails.visibility = View.VISIBLE
//            isGetAcademicYear()
//            Log.d("STANDARD&SECTION", "STANDARD&SECTION")
//            Log.d("SectionID", isSectionID.toString())
//            Log.d("ClassID", isClassID.toString())
//
//            appViewModel!!.getStudentReportDetails(
//                isAccessToken!!, class_id = isClassID!!, section_id = isSectionID!!, activity = this
//            )
//        }
////        //Called at initial because we load default Get ALL Student first
////        Log.d("InitialCall","InitialGetAllStudentData")
////        appViewModel!!.getStudentReportDetails(
////            isAccessToken!!, activity = this
////        )
//
////        appViewModel!!.getStudentReportDetails(
////            isAccessToken!!, isClassID!!, isSectionID!!, this
////        )
//    }
//
//    private fun loadStudentReport(studentReportData: List<StudentReportData>) {
//        // Once data is loaded, stop shimmer and pass the actual data
//        mAdapter =
//            StudentReportAdapter(studentReportData, this, this, Constant.isShimmerViewDisable)
//        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
//        // Set GridLayoutManager (2 columns in this case)
//        binding.rcyStudentReport.adapter = mAdapter
//    }
//
//    private fun isGetAcademicYear() {
//        Log.d("isGetAcademicYear", "Getting")
////        Constant.showLoading(this@StudentReport)
//
//        appViewModel!!.isGetAcademicYear(
//            isAccessToken!!, this
//        )
//    }
//
//    private fun isGetStandardSection() {
//        Log.d("isAcademicYearId", isAcademicYearId.toString())
////        Constant.showLoading(this@StudentReport)
//        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
//    }
//
//    private fun filter(text: String) {
//
//        val filteredList = if (text.isEmpty()) {
//            isStudentReportData
//        } else {
//            isStudentReportData.filter { it.name.contains(text, ignoreCase = true) }
//        }
//        (mAdapter).updateData(filteredList)
//
//    }
//
//
//    fun ErrorMessage(ErrorMessage: String) {
//        binding.rcyStudentReport.visibility = View.GONE
//        binding.lytNoDataFound.visibility = View.VISIBLE
//        binding.noDataFound.text = ErrorMessage
//    }
//
//    fun ShowData() {
//        binding.rcyStudentReport.visibility = View.VISIBLE
//        binding.lytNoDataFound.visibility = View.VISIBLE
//        binding.noDataFound.visibility = View.GONE
//    }
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.imgBack -> {
//                onBackPressed()
//            }
//
//            R.id.rlaSort -> {
//                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
//                    binding.dropdownTextView.text = selectedOption
//                    Log.d("SelectedFilterSort", binding.dropdownTextView.text.toString())
//
//                    // Reset to DefaultStandard  safely
//                    if (!isGetStandard.isNullOrEmpty()) {
//                        ShowData()
//                        val DefaultStandard = isGetStandard!![0]
//                        isClassID = DefaultStandard.id
//                        isSection = DefaultStandard.sections
//
//                        // Update Standard TextView
//                        binding.dropdownTextViewStandard.text = DefaultStandard.name
//
//                        //  Safely check if sections are available
//                        if (!DefaultStandard.sections.isNullOrEmpty()) {
//                            ShowData()
//                            isSectionID = DefaultStandard.sections[0].id
//                            binding.dropdownTextViewSection.text = DefaultStandard.sections[0].name
//                        } else {
//                            //  If no sections, reset section ID & TextView
//                            isSectionID = null
////                            binding.dropdownTextViewStandard.text = DefaultStandard.name
//                            binding.dropdownTextViewSection.text = "-"
//                            ErrorMessage("No Section Found in List")
//                            return@showDropdownMenuSort
//                        }
//                    } else {
//                        //  If no standards, reset IDs & TextViews
//                        isClassID = null
//                        isSectionID = null
//                        binding.dropdownTextViewStandard.text = "-"
//                        binding.dropdownTextViewSection.text = "-"
//                        ErrorMessage("No Standard And Section Found in List")
//                        //API call will dropped!,Because the ClassID and SectionID will have will be null
//                        return@showDropdownMenuSort
//                    }
//
//                    isGetStudentReport()
//                }
//
//            }
//
//            R.id.dropdownTextViewStandard -> {
//
//                showStandardDropdown(
//                    binding.dropdownTextViewStandard, this, isGetStandard
//                ) { selectStandard, position ->
//                    isClassID = selectStandard.id
////                    isSection = selectStandard.sections
////                    isSectionID=selectStandard.sections.get(0).id
//                    if (!selectStandard.sections.isNullOrEmpty()) {
//                        isSectionID = selectStandard.sections[0].id
//                        binding.dropdownTextViewSection.text = selectStandard.sections[0].name
//                    } else {
//                        //disable the dropdown because no section is there
//                        binding.dropdownTextViewSection.isEnabled = false
//                        binding.dropdownTextViewSection.isClickable = false
//                        // If no sections, reset section ID and clear TextView
//                        isSectionID = null
//                        binding.dropdownTextViewSection.text = "-"
//                        binding.dropdownTextViewSection
//                        ErrorMessage("No Section Found in List")
//                        return@showStandardDropdown
//                    }
////                    binding.dropdownTextViewSection.text=selectStandard.sections.get(0).name
//                    binding.dropdownTextViewStandard.text = selectStandard.name
//                    Log.d(
//                        "DropdownMenu",
//                        "Selected Standard: Name = ${selectStandard.name}, ID = ${selectStandard.id}, Position = $position"
//                    )
//                    isGetStudentReport()
//                }
//            }
//
//            R.id.dropdownTextViewSection -> {
//
//
//                isDropDownLoadDataSection(
//                    binding.dropdownTextViewSection,
//                    this,
//                    isSection
//                ) { selectedOption ->
//                    binding.dropdownTextViewSection.text = selectedOption.first
////                    isSectionId = selectedOption.second
//                    isSectionID = selectedOption.second
//                    isGetStudentReport()
//                }
//
//            }
//
//            R.id.AcademicYear -> {
//                showAcademicDropdown(
//                    binding.AcademicYear, this, isAcademicYear
//                ) { selectedYear ->
//                    binding.lblAcademicYear.text = selectedYear.year
//                    isGetStandardSection()
//
//                    Log.d(
//                        "DropdownMenu",
//                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
//                    )
//                    isGetStudentReport()
//                }
//
//            }
//
//
////We no longer need this animation because out of requiremnet
////            R.id.rlaStandard -> {
////                if (isVisibilityStandard) {
////                    isVisibilityStandard = false
////                    val animation = AnimationUtils.loadAnimation(
////                        applicationContext,
////                        R.anim.top_to_bottom_animation
////                    )
////                    binding.rlaStandardPicking.startAnimation(animation)
////                    binding.rlaStandardPicking.visibility = View.VISIBLE
////
////                } else {
////                    isVisibilityStandard = true
////                    val animation =
////                        AnimationUtils.loadAnimation(applicationContext, R.anim.slide_animation)
////                    binding.rlaStandardPicking.startAnimation(animation)
////
////                    handler.postDelayed({
////                        binding.rlaStandardPicking.visibility = View.GONE
////                    }, 400)
////                }
////            }
//        }
//    }
//
//    override fun onMailClick(data: StudentReportData) {
////        In Get Student Report API,We have not recived the Email-->10/05/2025
////        Constant.redirectToMail(this, data.email,"","")
//    }
//
//    override fun onPhoneClick(data: StudentReportData) {
//        Constant.redirectToDialPad(this, data.primary_mobile)
//    }
//
//    override fun onMessageClick(data: StudentReportData) {
//        Constant.redirectToMessage(this, data.primary_mobile)
//    }
//}

//existing code
//package com.vs.schoolmessenger.School.StudentReport
//
//import android.os.Handler
//import android.os.Looper
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import android.view.animation.AnimationUtils
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.databinding.StudentReportBinding
//
//class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,
//    StudentReportClickListener {
//
//    private lateinit var mAdapter: StudentReportAdapter
//    private lateinit var isStudentReportData: List<StudentReportData>
//
//    val items = listOf(
//        "Get all student",
//        "Student name (A - Z)",
//        "Student name (Z - A)",
//        "Roll number (0 - 1)",
//        "Roll number (1 - 0)"
//    )
//
//    val itemsStandard = listOf(
//        "XII",
//        "XI",
//        "X",
//        "IX",
//        "VII"
//    )
//    val itemsSection = listOf(
//        "A",
//        "B",
//        "C",
//        "D",
//        "E"
//    )
//
//    val handler = Handler(Looper.getMainLooper())
//
//    override fun getViewBinding(): StudentReportBinding {
//        return StudentReportBinding.inflate(layoutInflater)
//    }
//
//    private var isVisibilityStandard = true
//
//    override fun setupViews() {
//        super.setupViews()
//        setupToolbar()
//        binding.imgBack.setOnClickListener(this)
//        binding.rlaSort.setOnClickListener(this)
//        binding.rlaStandard.setOnClickListener(this)
//        binding.dropdownTextViewStandard.setOnClickListener(this)
//        binding.dropdownTextViewSection.setOnClickListener(this)
//
//
//        isStudentReportData = listOf(
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Sathish",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Bharath",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Partha",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Saran",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Murugan",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Vel",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Vijay",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Ajith",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            ),
//            StudentReportData(
//                "4783567951",
//                "Male",
//                "Apr 1 , 2001",
//                "Surya",
//                "Ganesan",
//                "Abi",
//                "6382677672",
//                "sathishg079@gmail.com",
//            )
//        )
//
//        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
//        binding.rcyStudentReport.adapter = mAdapter
//
//        Constant.executeAfterDelay {
//            // Once data is loaded, stop shimmer and pass the actual data
//            mAdapter =
//                StudentReportAdapter(isStudentReportData, this, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyStudentReport.adapter = mAdapter
//        }
//
//
//        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                filter(s.toString())
//            }
//        })
//    }
//
//    private fun filter(text: String) {
//
//        val filteredList = if (text.isEmpty()) {
//            isStudentReportData
//        } else {
//            isStudentReportData.filter { it.studentName.contains(text, ignoreCase = true) }
//        }
//        (mAdapter as StudentReportAdapter).updateData(filteredList)
//
//    }
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.imgBack -> {
//                onBackPressed()
//            }
//
//            R.id.rlaSort -> {
//
//                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
//                    binding.dropdownTextView.text = selectedOption
//                }
//            }
//
//            R.id.dropdownTextViewStandard -> {
//
//                showDropdownMenuSort(
//                    binding.dropdownTextViewStandard,
//                    this,
//                    itemsStandard
//                ) { selectedOption ->
//                    binding.dropdownTextViewStandard.text = selectedOption
//                    binding.lblStandard.text = selectedOption
//                }
//            }
//
//            R.id.dropdownTextViewSection -> {
//
//                showDropdownMenuSort(
//                    binding.dropdownTextViewSection,
//                    this,
//                    itemsSection
//                ) { selectedOption ->
//                    binding.dropdownTextViewSection.text = selectedOption
//                    binding.lblStandard.text =
//                        binding.dropdownTextViewStandard.text.toString() + " - " + selectedOption
//                }
//            }
//
//            R.id.rlaStandard -> {
//                if (isVisibilityStandard) {
//                    isVisibilityStandard = false
//                    val animation = AnimationUtils.loadAnimation(
//                        applicationContext,
//                        R.anim.top_to_bottom_animation
//                    )
//                    binding.rlaStandardPicking.startAnimation(animation)
//                    binding.rlaStandardPicking.visibility = View.VISIBLE
//
//                } else {
//                    isVisibilityStandard = true
//                    val animation =
//                        AnimationUtils.loadAnimation(applicationContext, R.anim.slide_animation)
//                    binding.rlaStandardPicking.startAnimation(animation)
//
//                    handler.postDelayed({
//                        binding.rlaStandardPicking.visibility = View.GONE
//                    }, 400)
//                }
//            }
//        }
//    }
//
//    override fun onMailClick(data: StudentReportData) {
//        Constant.redirectToMail(this, data.email,"","")
//    }
//
//    override fun onPhoneClick(data: StudentReportData) {
//        Constant.redirectToDialPad(this, data.mobileNumber)
//    }
//
//    override fun onMessageClick(data: StudentReportData) {
//        Constant.redirectToMessage(this, data.mobileNumber)
//    }
//}