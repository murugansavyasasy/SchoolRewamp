package com.vs.schoolmessenger.School.ClassTest.Standard

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Report.ExamReportActivity
import com.vs.schoolmessenger.School.ClassTest.Section.SectionActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectStandardCreateBinding

class StandardActivity : BaseActivity<SelectStandardCreateBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: StandardAdapter

    private var isClassList: List<StandardSection> = emptyList()

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false

    private var userDetails: UserDetails? = null

    override fun getViewBinding(): SelectStandardCreateBinding {
        return SelectStandardCreateBinding.inflate(layoutInflater)
    }

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
                binding.lytContent.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
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
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.staff_details?.find { it.school_id == instituteId }
            SharedPreference.putStaffDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        binding.imgBack.setOnClickListener(this)
        binding.viewreporttext.setOnClickListener(this)
        binding.btnAddTestMarks.setOnClickListener(this)
        setupStepIndicator()
        setupViewModel()
        setupContinueButton()
    }



    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 1)
    }

    private fun setupViewModel() {
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken  = isStaffDetails!!.access_token

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)

        if (!isAcademicYear.isNullOrEmpty()) {
            isAcademicYearId       = isAcademicYear!![0].id
            isCurrentAcademicYear  = isAcademicYear!![0].current_academic_year
            Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    buildStandardList(response.data)
                    showData()
                } else {
                    showError(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }
    }

    private fun buildStandardList(data: List<Standard>) {
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

        showStandardCards(standardSectionList.distinctBy { it.standardId })
    }

    private fun showStandardCards(uniqueStandards: List<StandardSection>) {

        if (uniqueStandards.isNotEmpty()) {
            Constant.isSelectedStandardId = uniqueStandards.first().standardId ?: ""
            Constant.isSelectedStandardName = uniqueStandards.first().standardName ?: ""
        }

        adapter = StandardAdapter(
            itemList = uniqueStandards,
            context = this,
            isLoading = false,
            fullSectionList = isClassList
        )

        binding.rcClassList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@StandardActivity.adapter
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
                    isAcademicYearId      = selected.id
                    isCurrentAcademicYear = selected.current_academic_year
                    Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
                    Log.d("DropdownMenu",
                        "Year: ID=${selected.id}, Year=${selected.year}, Current=${selected.current_academic_year}")
                    isGetStandardSection()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isGetStandardSection() {

        adapter = StandardAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcClassList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter       = this@StandardActivity.adapter
        }
        appViewModel!!.isGetStandardSection(isAccessToken!!, isAcademicYearId, this)
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            if (!::adapter.isInitialized || adapter.getSelectedStandardId() == null) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_a_standard_to_continue),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, SectionActivity::class.java))
        }
    }

    private fun showData() {
        binding.rcClassList.visibility = View.VISIBLE
        binding.lytList.visibility     = View.GONE
    }

    private fun showError(message: String) {
        binding.rcClassList.visibility = View.GONE
        binding.lytList.visibility     = View.VISIBLE
        binding.txtNoData.text         = message
    }

    private fun RedirectToReport() {
        val intent = Intent(this, ExamReportActivity::class.java)
        startActivity(intent)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.viewreporttext-> RedirectToReport()
            R.id.btnAddTestMarks-> RedirectToReport()
        }
    }
}