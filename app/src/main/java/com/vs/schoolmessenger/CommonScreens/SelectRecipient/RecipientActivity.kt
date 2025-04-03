package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.Section
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectRecipientBinding

class RecipientActivity : BaseActivity<SelectRecipientBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): SelectRecipientBinding {
        return SelectRecipientBinding.inflate(layoutInflater)
    }

    private var isSectionId: Int? = null
    var isGetSubjectListData: List<NameAndIds>? = null
    var isGetStandard: List<Standard>? = null
    var isSection: List<Section>? = null

    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(
            this
        )

        binding.rlaSection.setOnClickListener(
            this
        )

        binding.rlaStandard.setOnClickListener(
            this
        )

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Group"))
        tabLayout.addTab(tabLayout.newTab().setText("Standard"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Specific Student"))
        isGetSubjectList()
        isGetStandardSection()
        // isGetStudentList()

        appViewModel!!.isGetSubjectList?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    var isGetSubjectList = response.data
                    isGetSubjectListData = isGetSubjectList
                }
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    var isGetStandardData = response.data
                    isGetStandard = isGetStandardData
                }
            }
        }

        appViewModel!!.isStudentList?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {

                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaSubject -> {
                isDropDownLoadData(
                    binding.rlaSubject,
                    this,
                    isGetSubjectListData
                ) { selectedSubject ->
                    binding.lblSuibject.text = selectedSubject.first // Set name
                    Log.d(
                        "DropdownMenu",
                        "Selected Subject: Name = ${selectedSubject.first}, ID = ${selectedSubject.second}"
                    )
                }
            }

            R.id.rlaSection -> {
                showSectionDropdown(
                    binding.rlaSection,
                    this,
                    isSection
                ) { selectedSubject ->
                    binding.lblSection.text = selectedSubject.name // Set name
                    Log.d(
                        "DropdownMenu",
                        "Selected Subject: Name = ${selectedSubject.name}, ID = ${selectedSubject.id}"
                    )
                    isSectionId = selectedSubject.id
                }
            }

            R.id.rlaStandard -> {
                showStandardDropdown(
                    binding.rlaStandard,
                    this,
                    isGetStandard
                ) { selectStandard, position ->
                    binding.lblStandard.text = selectStandard.name // Set name
                    Log.d(
                        "DropdownMenu",
                        "Selected Standard: Name = ${selectStandard.name}, ID = ${selectStandard.id}, Position = $position"
                    )
                    isSection = selectStandard.sections
                }
            }
        }
    }

    private fun isGetSubjectList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetSubjectList(
            isToken!!.access_token, this
        )
    }

    private fun isGetStandardSection() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStandardSection(
            isToken!!.access_token, isSectionId.toString(), this
        )
    }

    private fun isGetStudentList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStudentList(
            isToken!!.access_token, isSectionId.toString(), this
        )
    }
}

