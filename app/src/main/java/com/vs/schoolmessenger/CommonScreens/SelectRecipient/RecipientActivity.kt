package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListAdapter
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListClickListener
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
    var isGetGroupListData: List<NameAndIds>? = null
    private var groupListAdapter: GroupListAdapter? = null
    var isGetStandard: List<Standard>? = null
    var isSection: List<Section>? = null

    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Group"))
        tabLayout.addTab(tabLayout.newTab().setText("Standard"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Specific Student"))

        // Set up RecyclerView and Adapter
        groupListAdapter = GroupListAdapter(object : GroupListClickListener {
            override fun onGroupClick(group: NameAndIds) {
                // Handle group click
            }
        }, this, true)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipientActivity)
            adapter = groupListAdapter
        }
        binding.recyclerView.visibility = View.GONE
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    1 -> {
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.rlaSection.visibility = View.GONE
                        binding.rlaStandard.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        binding.btnViewProgress.visibility = View.GONE
                    }

                    else -> {
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.VISIBLE
                        binding.rlaSection.visibility = View.VISIBLE
                        binding.rlaStandard.visibility = View.VISIBLE
                        binding.textdesc.visibility = View.GONE
                        binding.btnViewProgress.visibility = View.VISIBLE
                        binding.grouplabel.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        isGetSubjectList()
        isGetStandardSection()
        isGetGroupList()

        appViewModel!!.isGetGroupList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetGroupListData = response.data
                groupListAdapter?.updateData(isGetGroupListData!!)
            }
        }

        appViewModel!!.isGetSubjectList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetSubjectListData = response.data
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetStandard = response.data
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
                        "Selected Section: Name = ${selectedSubject.name}, ID = ${selectedSubject.id}"
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

    private fun isGetGroupList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetGroupList(isToken!!.access_token, this)
        appViewModel!!.isGetGroupList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetGroupListData = response.data
                groupListAdapter?.updateData(isGetGroupListData!!)
            }
        }
    }

    private fun isGetSubjectList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetSubjectList(isToken!!.access_token, this)
    }

    private fun isGetStandardSection() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStandardSection(isToken!!.access_token, isSectionId.toString(), this)
    }

    private fun isGetStudentList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStudentList(
            isToken!!.access_token, isSectionId.toString(), this
        )
    }
}

