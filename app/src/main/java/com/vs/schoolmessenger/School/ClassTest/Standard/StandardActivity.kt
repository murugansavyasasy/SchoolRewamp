package com.vs.schoolmessenger.School.ClassTest.Standard

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
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

    override fun getViewBinding(): SelectStandardCreateBinding {
        return SelectStandardCreateBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            binding.statusBarBackground.layoutParams.height =
                resources.getDimensionPixelSize(resourceId)
            binding.statusBarBackground.requestLayout()
        }
        binding.imgBack.setOnClickListener(this)
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
        adapter = StandardAdapter(
            itemList = uniqueStandards,
            context = this,
            isLoading = false,
            fullSectionList = isClassList
        )
        binding.rcClassList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter       = this@StandardActivity.adapter
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
                    "Please select a standard to continue",
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



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}