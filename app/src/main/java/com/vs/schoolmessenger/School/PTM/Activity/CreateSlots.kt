package com.vs.schoolmessenger.School.PTM.Activity

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.Adapter.SectionAndStandardAdapter
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CreateSlotsBinding

class CreateSlots : BaseActivity<CreateSlotsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateSlotsBinding {
        return CreateSlotsBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isAcademicYear: List<AcademicYear>? = null


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.lblOnline.setOnClickListener(this)
        binding.lblPhoneCall.setOnClickListener(this)
        binding.lblPerson.setOnClickListener(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isGetStandardSection()


        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    loadSectionStandard(response.data)
                }
            }
        }
    }

    private fun loadSectionStandard(data: List<Standard>) {
        val standardSectionList = mutableListOf<StandardSection>()

        for (standard in data) {
            for (section in standard.sections) {
                standardSectionList.add(
                    StandardSection(
                        standardName = standard.name,
                        sectionName = section.name
                    )
                )
            }
        }

        val adapter =
            SectionAndStandardAdapter(standardSectionList, this, Constant.isShimmerViewDisable)
        binding.rcySectionAndStandardList.layoutManager = GridLayoutManager(this, 5)
        binding.rcySectionAndStandardList.adapter = adapter

    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                val selectedOption = isAcademicYear!![position]
                isAcademicYearId = selectedOption.id
                isCurrentAcademicYear = selectedOption.current_academic_year
                Log.d(
                    "DropdownMenu",
                    "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                )
                isGetStandardSection()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblPerson -> {
                isChangeTheBackRound(binding.lblPerson)
            }

            R.id.lblOnline -> {
                isChangeTheBackRound(binding.lblOnline)
            }

            R.id.lblPhoneCall -> {
                isChangeTheBackRound(binding.lblPhoneCall)
            }
        }
    }

    private fun isChangeTheBackRound(isSelectedTextView: TextView) {
        binding.lblPerson.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblOnline.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))
        binding.lblPhoneCall.setBackgroundDrawable(this.getDrawable(R.drawable.gray_bg_radius))

        isSelectedTextView.setBackgroundDrawable(this.getDrawable(R.drawable.bg_light_blue))

    }
}