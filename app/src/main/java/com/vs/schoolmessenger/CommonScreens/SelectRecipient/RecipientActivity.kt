package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListClickListener
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectRecipientBinding

class RecipientActivity : BaseActivity<SelectRecipientBinding>(),
    View.OnClickListener, SectionListClickListener, StandardListClickListener,
    GroupListClickListener {

    override fun getViewBinding(): SelectRecipientBinding {
        return SelectRecipientBinding.inflate(layoutInflater)
    }

    var isDropDown = false
    private var isSectionId: Int? = null
    var isGetSubjectListData: List<NameAndIds>? = null
    var isGetGroupListData: List<NameAndIds>? = null
    private var isSectionAdapter: SectionListAdapter? = null
    private var isStandardListAdapter: StandardListAdapter? = null
    var isGetStandard: List<Standard>? = null
    var isSection: List<Section>? = null
    private var groupListAdapter: GroupListAdapter? = null


    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"), true)
        tabLayout.addTab(tabLayout.newTab().setText("Group"))
        tabLayout.addTab(tabLayout.newTab().setText("Standard"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Specific Student"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("tab?.position", tab?.position.toString())
                when (tab?.position) {
                    0 -> {
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.VISIBLE
                    }

                    1 -> {
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        isGetGroupList()

                    }

                    2 -> {
                        isDropDown = false
                        isGetStandardSection()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.text = "Standard"
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        Log.d("isDropDown", isDropDown.toString())
                        if (!isDropDown) {
                            binding.recyclerView.visibility = View.VISIBLE

                        } else {
                            binding.recyclerView.visibility = View.GONE
                        }
                    }

                    else -> {
                        isDropDown = true
                        isGetStandardSection()
                        if (isDropDown) {
                            binding.rlaStandard.visibility = View.VISIBLE
                        } else {
                            binding.rlaStandard.visibility = View.GONE
                        }
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.VISIBLE
                        binding.rlaStandard.visibility = View.VISIBLE
                        binding.textdesc.visibility = View.GONE
                        binding.btnViewProgress.visibility = View.VISIBLE
                        binding.grouplabel.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        appViewModel!!.isGetGroupList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetGroupListData = response.data
                isLoadGroupData(isGetGroupListData)
            }
        }

        appViewModel!!.isGetSubjectList?.observe(this) { response ->
            if (response != null && response.status) {
                binding.rlaSubject.visibility = View.VISIBLE
                isGetSubjectListData = response.data
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null && response.status) {
                isGetStandard = response.data
                if (!isDropDown) {
                    isLoadTheStandardData(isGetStandard)
                }
            }
        }
    }

    private fun isLoadGroupData(isGetGroupListData: List<NameAndIds>?) {
        groupListAdapter = GroupListAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = groupListAdapter
        Constant.executeAfterDelay {
            groupListAdapter = GroupListAdapter(
                isGetGroupListData,
                this@RecipientActivity,
                this,
                Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = groupListAdapter
        }
    }

    private fun isLoadData(isSection: List<Section>?) {

        isSectionAdapter = SectionListAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = isSectionAdapter
        Constant.executeAfterDelay {
            isSectionAdapter = SectionListAdapter(
                isSection,
                this@RecipientActivity,
                this,
                Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = isSectionAdapter
        }
    }

    private fun isLoadTheStandardData(isGetStandard: List<Standard>?) {
        isStandardListAdapter = StandardListAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = isStandardListAdapter
        Constant.executeAfterDelay {
            isStandardListAdapter = StandardListAdapter(
                isGetStandard,
                this,
                this,
                Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = isStandardListAdapter
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
                    binding.recyclerView.visibility = View.VISIBLE
                    isLoadData(isSection)
                }
            }
        }
    }

    private fun isGetGroupList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetGroupList(isToken!!.access_token, this)
    }

    private fun isGetSubjectList(isSectionId: Int?) {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetSubjectList(isToken!!.access_token, isSectionId.toString(), this)
    }

    private fun isGetStandardSection() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStandardSection(isToken!!.access_token.toString(), this)
    }

    private fun isGetStudentList() {
        val isToken = SharedPreference.getStaffDetails(this)
        appViewModel!!.isGetStudentList(
            isToken!!.access_token, isSectionId.toString(), this
        )
    }


    override fun onSectionClick(
        data: Section,
        isChecked: Boolean
    ) {
        Log.d("data.id", data.id.toString())
        isSectionId = data.id
        isGetSubjectList(isSectionId)
    }

    override fun onStandardClick(
        data: Standard,
        isChecked: Boolean
    ) {

    }

    override fun onGroupClick(group: NameAndIds) {

    }
}

