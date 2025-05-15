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
        "Get all student",
        "Student name (A - Z)",
        "Student name (Z - A)",
        "Roll number (0 - 1)",
        "Roll number (1 - 0)"
    )

    val itemsStandard = listOf(
        "XII",
        "XI",
        "X",
        "IX",
        "VII"
    )
    val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E"
    )

    val handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    private var isVisibilityStandard = true

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.imgBack.setOnClickListener(this)
        binding.rlaSort.setOnClickListener(this)

//        binding.rlaStandard.setOnClickListener(this)
        binding.dropdownTextViewStandard.setOnClickListener(this)
        binding.dropdownTextViewSection.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetAcademicYear()
        isGetStudentReport()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
            Log.d("AcademicYearResponse",response?.data.toString())
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year == true } == true
                binding.lblAcademicYear.text = isAcademicYear!![0].year
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                Log.d("isAcademicYearId",isAcademicYearId.toString())
                isGetStandardSection()
            }
        }
        appViewModel!!.isStandardSectionList?.observe(this) { response ->
//            Constant.hideLoading(this@StudentReport)
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        isClassID=isGetStandard!!.get(0).id
                        Log.d("isClassID",isClassID.toString())
                        isSectionId = isGetStandard!!.get(0).sections.get(0).id
                        binding.dropdownTextViewStandard.text = isGetStandard!!.get(0).name
                        if (isGetStandard!!.get(0).sections.size > 0) {
                            binding.dropdownTextViewSection.text = isGetStandard!!.get(0).sections.get(0).name
                            isSection = isGetStandard!!.get(0).sections
                            Log.d("isSectionID",isSection.toString())
                        }
                    } else {
                        binding.dropdownTextViewStandard.visibility = View.GONE
                        binding.dropdownTextViewSection.visibility = View.GONE
                    }
                }
            }
        }


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




        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })
    }

    private fun isGetStudentReport() {
        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                StudentReportAdapter(isStudentReportData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyStudentReport.adapter = mAdapter
        }
        Log.d("ClassAndSectionID",isClassID.toString()+isSectionID.toString())
        appViewModel!!.getStudentReportDetails(
            isAccessToken!!,isClassID!!,isSectionID!!, this
        )
    }
    private fun isGetAcademicYear() {
//        Constant.showLoading(this@StudentReport)

        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }
    private fun isGetStandardSection() {
        Log.d("isAcademicYearId",isAcademicYearId.toString())
//        Constant.showLoading(this@StudentReport)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun filter(text: String) {

        val filteredList = if (text.isEmpty()) {
            isStudentReportData
        } else {
            isStudentReportData.filter { it.name.contains(text, ignoreCase = true) }
        }
        (mAdapter as StudentReportAdapter).updateData(filteredList)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaSort -> {

                showDropdownMenuSort(binding.dropdownTextView, this, items) { selectedOption ->
                    binding.dropdownTextView.text = selectedOption
                }
            }

            R.id.dropdownTextViewStandard -> {

                showStandardDropdown(
                    binding.dropdownTextViewStandard, this, isGetStandard
                ) { selectStandard, position ->
                    binding.dropdownTextViewStandard.text = selectStandard.name
                    isSection = selectStandard.sections
                    Log.d(
                        "DropdownMenu",
                        "Selected Standard: Name = ${selectStandard.name}, ID = ${selectStandard.id}, Position = $position"
                    )
                    isGetStudentReport()
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
                    isGetStudentReport()
                }

            }

            R.id.dropdownTextViewSection -> {


                isDropDownLoadDataSection(
                    binding.dropdownTextViewSection,
                    this,
                    isSection
                ) { selectedOption ->
                    binding.dropdownTextViewSection.text = selectedOption.first
                    isSectionId = selectedOption.second
                    isGetStudentReport()
                }

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
//        Constant.redirectToMail(this, data.email,"","")
    }

    override fun onPhoneClick(data: StudentReportData) {
        Constant.redirectToDialPad(this, data.primary_mobile)
    }

    override fun onMessageClick(data: StudentReportData) {
        Constant.redirectToMessage(this, data.primary_mobile)
    }
}

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