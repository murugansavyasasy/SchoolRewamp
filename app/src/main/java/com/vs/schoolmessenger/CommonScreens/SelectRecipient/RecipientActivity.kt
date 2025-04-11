package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectRecipientBinding

class RecipientActivity : BaseActivity<SelectRecipientBinding>(), View.OnClickListener,
    SectionListClickListener, StandardListClickListener, GroupListClickListener {

    override fun getViewBinding(): SelectRecipientBinding {
        return SelectRecipientBinding.inflate(layoutInflater)
    }
    val isGroupSelectedIds = mutableListOf<NameAndIds>()
    val isStandardSelectedIds = mutableListOf<Standard>()

    var isDropDown = false
    private var isSectionId: Int? = null
    var isGetSubjectListData: List<NameAndIds>? = null
    var isGetGroupListData: List<NameAndIds>? = null
    private var isSectionAdapter: SectionListAdapter? = null
    private var isStandardListAdapter: StandardListAdapter? = null
    var isGetStandard: List<Standard>? = null
    var isSection: List<Section>? = null
    private var groupListAdapter: GroupListAdapter? = null
    private var isAccessToken: String? = null

    private var isUserDetails: UserDetails? = null
    private var selectedIds = mutableListOf<Int>()
    var isSelectedType = 0


    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"))
        tabLayout.addTab(tabLayout.newTab().setText("Groups"))
        tabLayout.addTab(tabLayout.newTab().setText("Standards"))
        tabLayout.addTab(tabLayout.newTab().setText("Staffs"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Student"))

        val isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.lblSchoolName.text = isStaffDetails.school_name

        isUserDetails = SharedPreference.getUserDetails(this)
        if (isUserDetails!!.staff_role == Constant.isGroupHeadRole || isUserDetails!!.staff_role == Constant.isPrincipalRole || isUserDetails!!.staff_role == Constant.isAdminRole) {
            binding.btnViewProgress.visibility = View.GONE
            binding.textdesc.visibility = View.VISIBLE
            if (isUserDetails!!.staff_details.size > 1) {
                tabLayout.post {
                    tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                    tabLayout.getTabAt(1)?.select()
                }
            }
        } else {
            tabLayout.getTabAt(1)?.select()
            tabLayout.getTabAt(0)?.view?.visibility = View.GONE
            tabLayout.getTabAt(3)?.view?.visibility = View.GONE
            binding.btnViewProgress.visibility = View.VISIBLE
            binding.textdesc.visibility = View.VISIBLE
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        isSelectedType = 0
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        selectedIds.clear()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.VISIBLE
                    }

                    1 -> {
                        isSelectedType = 1
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        selectedIds.clear()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        isGetGroupList()

                    }

                    2 -> {
                        isSelectedType = 2
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        selectedIds.clear()
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
                    3 -> {
                        isSelectedType = 3
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        selectedIds.clear()
                        isDropDown = false
                        isGetStaffList()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.text = "Staff's"
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
                        isSelectedType = 4
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        selectedIds.clear()
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

        appViewModel!!.isGetStaffList?.observe(this) { response ->
            if (response != null && response.status) {
                isLoadStaffData(response.data)
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }
    }

    private fun isLoadStaffData(data: List<NameAndIds>) {
        groupListAdapter = GroupListAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = groupListAdapter
        Constant.executeAfterDelay {
            groupListAdapter = GroupListAdapter(
                data, this@RecipientActivity, this, Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = groupListAdapter
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
                isGetGroupListData, this@RecipientActivity, this, Constant.isShimmerViewDisable
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
                isSection, this@RecipientActivity, this, Constant.isShimmerViewDisable
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
                isGetStandard, this, this, Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = isStandardListAdapter
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaSubject -> {
                isDropDownLoadData(
                    binding.rlaSubject, this, isGetSubjectListData
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
                    binding.rlaStandard, this, isGetStandard
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
            R.id.btnSend -> {
                var isTypeOfName = ""
                if (isSelectedType == 0) {
                    isTypeOfName = "School"
                } else if (isSelectedType == 1) {
                    isTypeOfName = "Group"
                    selectedIds = isGroupSelectedIds.map { it.id }.toMutableList()
                } else if (isSelectedType == 2) {
                    isTypeOfName = "Standard"
                    selectedIds = isStandardSelectedIds.map { it.id }.toMutableList()
                } else if (isSelectedType == 3) {
                    isTypeOfName = "Staff"
                } else if (isSelectedType == 4) {

                }

                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {
                    showSendConfirmationDialog("Are you want send this voice?")
                } else {
                    Constant.showAlert("Alert!", "Select atleast one $isTypeOfName", this)
                }
            }
        }
    }

    private fun isGetGroupList() {
        appViewModel!!.isGetGroupList(isAccessToken!!, this)
    }

    private fun isGetSubjectList(isSectionId: Int?) {
        appViewModel!!.isGetSubjectList(isAccessToken!!, isSectionId.toString(), this)
    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), this)
    }

    private fun isGetStudentList() {
        appViewModel!!.isGetStudentList(
            isAccessToken!!, isSectionId.toString(), this
        )
    }

    private fun isGetStaffList() {
        appViewModel!!.isGetStaffList(
            isAccessToken!!, this
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(isMessage: String) {

        var isTargetType: Int? = null
        var isCircularType: String? = null
        if (isSelectedType == 0) {
            isTargetType = Constant.isSchool
            isCircularType = Constant.school
        } else if (isSelectedType == 1) {
            isTargetType = Constant.isGroup
            isCircularType = Constant.group
            selectedIds = isGroupSelectedIds.map { it.id }.toMutableList()
        } else if (isSelectedType == 2) {
            isTargetType = Constant.isStandard
            isCircularType = Constant.standard
            selectedIds = isStandardSelectedIds.map { it.id }.toMutableList()
        } else if (isSelectedType == 3) {
            isTargetType = Constant.isStaff
            isCircularType = Constant.staff
        } else if (isSelectedType == 4) {
            isTargetType = Constant.isStudent
            isCircularType = Constant.student
        }

        val isVoiceData = Constant.isVoiceSendingData
        AlertDialog.Builder(this).setTitle("Send Confirmation!").setMessage(isMessage)
            .setPositiveButton("Yes") { dialog, _ ->
                val isVoiceUrl =
                    "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/2025-04-09/5512/audiorecord.m4a"
                val jsonObject = ApiCallRequest.isVoiceSend(
                    isFileUploaded = isVoiceUrl,
                    isClickType = isVoiceData!!.isClickType,
                    selectedDates = isVoiceData.selectedDates,
                    isStartTimeText = isVoiceData.isStartTimeText,
                    isEndTimeText = isVoiceData.isEndTimeText,
                    title = isVoiceData.title,
                    isEmergency = isVoiceData.isEmergency,
                    isScheduleCall = isVoiceData.isScheduleCall,
                    schoolId = selectedIds,
                    targetType = isTargetType!!,
                    circularType = isCircularType!!
                )
                appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    override fun onSectionClick(
        data: Section, isChecked: Boolean
    ) {
        Log.d("data.id", data.id.toString())
        isSectionId = data.id
        isGetSubjectList(isSectionId)
    }


    override fun onIdCheck(group: NameAndIds) {
        if (!isGroupSelectedIds.any { it.id == group.id }) {
            isGroupSelectedIds.add(group)
        }
    }

    override fun onIdUnchecked(group: NameAndIds) {
        isGroupSelectedIds.removeAll { it.id == group.id }
    }

    override fun onIdCheck(isStandard: Standard) {
        if (!isStandardSelectedIds.any { it.id == isStandard.id }) {
            isStandardSelectedIds.add(isStandard)
        }
    }

    override fun onIdUnchecked(isStandard: Standard) {
        isStandardSelectedIds.removeAll { it.id == isStandard.id }
    }
}

