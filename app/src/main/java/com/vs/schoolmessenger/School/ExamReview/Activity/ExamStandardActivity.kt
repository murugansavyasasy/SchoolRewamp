package com.vs.schoolmessenger.School.ExamReview.Activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SpecificStudent.SpecificStudentSelectClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Report.ExamReportActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.ExamReview.Adapter.ExamStandardSectionDropdownAdapter
import com.vs.schoolmessenger.School.ExamReview.Adapter.StudentAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamStandardSelectBinding
import kotlin.jvm.java


class ExamStandardActivity : BaseActivity<ExamStandardSelectBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true

    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private var isClassList: List<StandardSection> = emptyList()
    private var selectedStandardSection: StandardSection? = null

    private lateinit var studentAdapter: StudentAdapter
    private var selectedStudent: NameAndIds? = null

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var isSelectedStudent: NameAndIds? = null

    override fun getViewBinding(): ExamStandardSelectBinding =
        ExamStandardSelectBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        Constant.clearClassTestFlowData()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.statusBarBackground.layoutParams.height = statusBarHeight
            binding.statusBarBackground.requestLayout()

            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val baseBottomMargin = resources.getDimensionPixelSize(R.dimen.twenty)
            val lytContentParams =
                binding.lytContent.layoutParams as ConstraintLayout.LayoutParams
            lytContentParams.bottomMargin = baseBottomMargin + navBarHeight
            binding.lytContent.layoutParams = lytContentParams
            insets
        }

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            binding.statusBarBackground.layoutParams.height =
                resources.getDimensionPixelSize(resourceId)
            binding.statusBarBackground.requestLayout()
        }

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
        if (fromNotification) {
            Constant.isParentChoose = false
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            instituteId = intent.getStringExtra(Constant.institute_id)
            menu_name = intent.getStringExtra(Constant.menu_name)
            val matchedChild = userDetails?.staff_details?.find { it.school_id == instituteId }
            SharedPreference.putStaffDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        binding.imgBack.setOnClickListener(this)
        binding.viewreporttext.setOnClickListener(this)
        binding.btnAddTestMarks.setOnClickListener(this)

        setupStepIndicator()
        setupViewModel()
        setupStandardSectionDropdown()
        setupStudentList()
        setupContinueButton()
    }

    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 1)
    }

    private fun setupViewModel() {
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)

        if (!isAcademicYear.isNullOrEmpty()) {
            isAcademicYearId = isAcademicYear!![0].id
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
            Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    buildStandardSectionList(response.data)
                } else {
                    Toast.makeText(
                        this,
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        appViewModel!!.isStudentList?.observe(this) { response ->
            if (response != null) {
                studentAdapter.setLoading(false)
                if (response.status && response.data.isNotEmpty()) {
                    showStudentData()
                    studentAdapter.updateList(response.data)
                } else {
                    showStudentError(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }
    }

    private fun buildStandardSectionList(data: List<Standard>) {
        val standardSectionList = mutableListOf<StandardSection>()
        for (standard in data) {
            for (section in standard.sections) {
                standardSectionList.add(
                    StandardSection(
                        standardId = standard.id.toString(),
                        standardName = standard.name,
                        sectionId = section.id.toString(),
                        sectionName = section.name
                    )
                )
            }
        }
        isClassList = standardSectionList
        Constant.isAllStandardSections = standardSectionList

        binding.lytStandardSectionDropdown.isEnabled = standardSectionList.isNotEmpty()

        if (standardSectionList.isNotEmpty()) {
            onStandardSectionSelected(standardSectionList.first())
        } else {
            binding.txtSelectedStandardSection.text = getString(R.string.select_standard_section)
        }
    }

    private fun setupStandardSectionDropdown() {
        binding.lytStandardSectionDropdown.setOnClickListener {
            if (isClassList.isEmpty()) return@setOnClickListener
            showStandardSectionPicker()
        }
    }

    private fun showStandardSectionPicker() {
        val dialog = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this)
            .inflate(R.layout.bottomsheet_standard_section_picker, null)
        dialog.setContentView(sheetView)

        val rcPicker = sheetView.findViewById<RecyclerView>(R.id.rcStandardSectionPicker)
        rcPicker.layoutManager = LinearLayoutManager(this)
        rcPicker.adapter = ExamStandardSectionDropdownAdapter(
            items = isClassList,
            selectedSectionId = selectedStandardSection?.sectionId
        ) { picked ->
            onStandardSectionSelected(picked)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun onStandardSectionSelected(item: StandardSection) {
        selectedStandardSection = item
        binding.txtSelectedStandardSection.text =
            "${item.standardName} - ${item.sectionName}"

        Constant.isSelectedStandardId = item.standardId ?: ""
        Constant.isSelectedStandardName = item.standardName ?: ""
        Constant.isSelectedSections = listOf(item)
        selectedStudent = null
        binding.btnContinue.isEnabled = false

        loadStudentsForSection(item.sectionId)
    }

    private fun loadStudentsForSection(sectionId: String?) {
        if (sectionId.isNullOrEmpty() || isAccessToken.isNullOrEmpty()) return
        showStudentLoading()
        appViewModel!!.isGetStudentList(isAccessToken!!, sectionId, isAcademicYearId, this)
    }

    private fun setupStudentList() {
        studentAdapter = StudentAdapter(
            itemList = emptyList(),
            listener = object : SpecificStudentSelectClickListener {
                override fun onIdCheck(data: NameAndIds) {
                    selectedStudent = data
                    binding.btnContinue.isEnabled = true
                }

                override fun onIdUnchecked(data: NameAndIds) {
                    if (selectedStudent?.id == data.id) {
                        selectedStudent = null
                        binding.btnContinue.isEnabled = false
                    }
                }
            },
            context = this,
            isLoading = false,
            isSingleSelect = true
        )

        binding.rcStudentList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = studentAdapter
        }
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val spinnerAdapter = NewAcademicYearAdapter(this, isAcademicYear)
        binding.isAcademicSpinner.adapter = spinnerAdapter
        binding.isAcademicSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    spinnerAdapter.selectedPosition = position
                    val selected = isAcademicYear!![position]
                    isAcademicYearId = selected.id
                    isCurrentAcademicYear = selected.current_academic_year
                    Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
                    isGetStandardSection()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isGetStandardSection() {
        selectedStandardSection = null
        selectedStudent = null
        binding.txtSelectedStandardSection.text = getString(R.string.select_standard_section)
        binding.btnContinue.isEnabled = false
        studentAdapter.updateList(emptyList())
        appViewModel!!.isGetStandardSection(isAccessToken!!, isAcademicYearId, this)
    }

    private fun setupContinueButton() {
        binding.btnContinue.isEnabled = false
        binding.btnContinue.setOnClickListener {
            if (selectedStandardSection == null) {
                Toast.makeText(this, "Please select a standard & section", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedStudent == null) {
                Toast.makeText(this, "Please select a student to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Constant.isSelectedStudent = selectedStudent
            startActivity(Intent(this, SelectExamActivity::class.java))
        }
    }
    private fun showStudentLoading() {
        binding.rcStudentList.visibility = View.VISIBLE
        binding.lytStudentEmpty.visibility = View.GONE
        studentAdapter.setLoading(true)
    }

    private fun showStudentData() {
        binding.rcStudentList.visibility = View.VISIBLE
        binding.lytStudentEmpty.visibility = View.GONE
    }

    private fun showStudentError(message: String) {
        binding.rcStudentList.visibility = View.GONE
        binding.lytStudentEmpty.visibility = View.VISIBLE
        binding.txtStudentNoData.text = message
    }

    private fun RedirectToReport() {
        startActivity(Intent(this, ExamReportActivity::class.java))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.viewreporttext -> RedirectToReport()
            R.id.btnAddTestMarks -> RedirectToReport()
        }
    }
}