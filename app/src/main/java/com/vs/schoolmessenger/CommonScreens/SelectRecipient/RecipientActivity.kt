package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupStaffAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListClickListener
import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudent
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.Constant.SH_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.SH_HOMEWORK
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectRecipientBinding

class RecipientActivity : BaseActivity<SelectRecipientBinding>(), View.OnClickListener,
    SectionListClickListener, StandardListClickListener, GroupListClickListener {

    override fun getViewBinding(): SelectRecipientBinding {
        return SelectRecipientBinding.inflate(layoutInflater)
    }
    val isGroupSelectedIds = mutableListOf<NameAndIds>()
    val isStandardSelectedIds = mutableListOf<Standard>()
    val isSectionSelectedIds = mutableListOf<Section>()
    var isDropDown = false
    private var isSectionId = mutableListOf<Int>()
    var isGetSubjectListData: List<NameAndIds>? = null
    var isGetGroupListData: List<NameAndIds>? = null
    var isGetStaffListData: List<NameAndIds>? = null
    var isAcademicYear: List<AcademicYear>? = null
    private var isSectionAdapter: SectionListAdapter? = null
    private var isStandardListAdapter: StandardListAdapter? = null
    var isGetStandard: List<Standard>? = null
    var isSection: List<Section>? = null
    private var isGroupStaffAdapter: GroupStaffAdapter? = null
    private var isAccessToken: String? = null
    private var isUserDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null
    private var selectedIds = mutableListOf<Int>()
    var isSelectedType = 0
    var isAcademicYearId = -1
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isCurrentAcademicYear = true
    var isTargetType: Int? = null
    var isCircularType: String? = null

    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.rlaAcademicYear.setOnClickListener(this)
        binding.btnSpecificStudent.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Entire School"))
        tabLayout.addTab(tabLayout.newTab().setText("Standard"))
        tabLayout.addTab(tabLayout.newTab().setText("Section/Student"))
        tabLayout.addTab(tabLayout.newTab().setText("Groups"))
        tabLayout.addTab(tabLayout.newTab().setText("Staff"))

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.lblSchoolName.text = isStaffDetails!!.school_name

        if (isStaffDetails!!.school_name_regional != ""){
            binding.lblSchoolRegionalName.visibility= View.VISIBLE
            binding.lblSchoolRegionalName.text = isStaffDetails!!.school_name_regional
        }else{
            binding.lblSchoolRegionalName.visibility= View.GONE
        }

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isUserDetails = SharedPreference.getUserDetails(this)
        isGetAcademicYear()
        tapVisibility(tabLayout)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.chAllSelect.isChecked = false
                        binding.chAllSelect.visibility = View.GONE
                        isSelectedType = 0
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        isSectionSelectedIds.clear()
                        selectedIds.clear()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.VISIBLE
                        binding.btnSpecificStudent.visibility = View.GONE
                    }

                    1 -> {

                        isSelectedType = 1
                        binding.chAllSelect.visibility = View.GONE
                        binding.chAllSelect.isChecked = false
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        isSectionSelectedIds.clear()
                        selectedIds.clear()
                        isDropDown = false
                        isGetStandardSection()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.text = "Standard"
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        binding.btnSpecificStudent.visibility = View.GONE
                        Log.d("isDropDown", isDropDown.toString())
                        if (!isDropDown) {
                            binding.recyclerView.visibility = View.VISIBLE
                        } else {
                            binding.recyclerView.visibility = View.GONE
                        }
                    }

                    2 -> {
                        binding.chAllSelect.isChecked = false
                        isSelectedType = 2
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        isSectionSelectedIds.clear()
                        selectedIds.clear()
                        isDropDown = true
                        isGetStandardSection()
//                        if (isDropDown) {
//                            binding.rlaStandard.visibility = View.VISIBLE
//                        } else {
//                            binding.rlaStandard.visibility = View.GONE
//                        }
                        binding.recyclerView.visibility = View.GONE
//                        binding.rlaStandard.visibility = View.VISIBLE
                        binding.rlaStandard.visibility = View.GONE

                        binding.textdesc.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.chAllSelect.visibility = View.GONE
                        binding.rlaSubject.visibility = View.GONE
                        binding.btnSpecificStudent.visibility = View.VISIBLE
                        binding.btnSpecificStudent.isEnabled = false
                        binding.btnSpecificStudent.background =
                            ContextCompat.getDrawable(this@RecipientActivity, R.drawable.bg_gray)
                    }

                    3 -> {

                        binding.chAllSelect.visibility = View.GONE
                        binding.chAllSelect.isChecked = false
                        isSelectedType = 3
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        isSectionSelectedIds.clear()
                        binding.grouplabel.text = "Groups"
                        selectedIds.clear()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.btnSpecificStudent.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        if (isAcademicYearId != -1) {
                            isGetGroupList()
                        }

                    }

                    4 -> {
                        binding.chAllSelect.isChecked = false
                        isSelectedType = 4
                        isGroupSelectedIds.clear()
                        isStandardSelectedIds.clear()
                        isSectionSelectedIds.clear()
                        selectedIds.clear()
                        isDropDown = false
                        isGetStaffList()
                        binding.rlaStandard.visibility = View.GONE
                        binding.grouplabel.text = "Staff"
                        binding.grouplabel.visibility = View.VISIBLE
                        binding.rlaSubject.visibility = View.GONE
                        binding.chAllSelect.visibility = View.GONE
                        binding.textdesc.visibility = View.GONE
                        binding.btnSpecificStudent.visibility = View.GONE
                        Log.d("isDropDown", isDropDown.toString())
                        if (!isDropDown) {
                            binding.recyclerView.visibility = View.VISIBLE
                        } else {
                            binding.recyclerView.visibility = View.GONE
                        }


                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            if (response != null) {
                response.data.let { academicList ->
                    val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                    isAcademicYear = reorderedList
                    binding.lblAcademicYear.text = isAcademicYear!![0].year
                    isAcademicYearId = isAcademicYear!![0].id
                    isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
//                    isGetGroupList()
                    if (isSelectedType != 0) {
                        isGetStandardSection()
                    }
                }
            }
        }

        appViewModel!!.isGetGroupList?.observe(this) { response ->
            if (response != null) {
                isGetGroupListData = response.data
                if (isGetGroupListData!!.isNotEmpty()) {
                    binding.lblNoRecord.visibility = View.GONE
                    binding.chAllSelect.visibility = View.VISIBLE
                    binding.grouplabel.visibility = View.VISIBLE
                } else {
                    binding.lblNoRecord.visibility = View.VISIBLE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.lblNoRecord.text = response.message
                }
                isLoadGroupData(isGetGroupListData)
            }
        }

        appViewModel!!.isGetSubjectList?.observe(this) { response ->
            if (response != null) {
                // Please don't delete by sathish
//                binding.rlaSubject.visibility = View.VISIBLE
//                isGetSubjectListData = response.data
//                isLoadSubjectData()
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                isGetStandard = response.data
                if (isGetStandard!!.isNotEmpty()) {
                    binding.lblNoRecord.visibility = View.GONE
                    if (isSelectedType != 1) {
                        binding.rlaStandard.visibility = View.VISIBLE
                        isSection = isGetStandard!!.get(0).sections
                        binding.lblStandard.text = isGetStandard!![0].name
                        binding.recyclerView.visibility = View.VISIBLE
                        isLoadData(isSection)

                    }
                    binding.chAllSelect.visibility = View.VISIBLE
                    binding.grouplabel.visibility = View.VISIBLE
                } else {
                    binding.lblNoRecord.visibility = View.VISIBLE
                    binding.rlaStandard.visibility = View.GONE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.lblNoRecord.text = response.message
                }
                if (!isDropDown) {
                    isLoadTheStandardData(isGetStandard)
                }
            }
        }

        appViewModel!!.isGetStaffList?.observe(this) { response ->
            if (response != null) {
                isGetStaffListData = response.data

                if (isGetStaffListData!!.isNotEmpty()) {
                    binding.lblNoRecord.visibility = View.GONE
                    binding.chAllSelect.visibility = View.VISIBLE
                    binding.grouplabel.visibility = View.VISIBLE
                } else {
                    binding.lblNoRecord.visibility = View.VISIBLE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.lblNoRecord.text = response.message
                }
                isLoadStaffData(response.data)
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }
        appViewModel!!.isSendText?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }

        binding.chAllSelect.setOnClickListener {
            if (isSelectedType == 1) {
                if (binding.chAllSelect.isChecked) {
                    isStandardListAdapter!!.selectAll()
                    isStandardListAdapter!!.itemList?.forEach { item ->
                        onIdCheck(item)
                    }
                } else {
                    isStandardListAdapter!!.deselectAll()
                    isStandardListAdapter!!.itemList?.forEach { item ->
                        onIdUnchecked(item)
                    }
                }
            } else if (isSelectedType == 2) {
                if (binding.chAllSelect.isChecked) {
                    isSectionAdapter!!.selectAll()
                    isSectionAdapter!!.itemList?.forEach { item ->
                        onIdCheck(item)
                    }
                } else {
                    isSectionAdapter!!.deselectAll()
                    isSectionAdapter!!.itemList?.forEach { item ->
                        onIdUnchecked(item)
                    }
                }
            } else if (isSelectedType == 3) {
                if (binding.chAllSelect.isChecked) {
                    isGroupStaffAdapter!!.selectAll()
                    isGroupStaffAdapter!!.itemList?.forEach { item ->
                        onIdCheck(item)
                    }
                } else {
                    isGroupStaffAdapter!!.deselectAll()
                    isGroupStaffAdapter!!.itemList?.forEach { item ->
                        onIdUnchecked(item)
                    }
                }
            }else if (isSelectedType == 4) {
                if (binding.chAllSelect.isChecked) {
                    isGroupStaffAdapter!!.selectAll()
                    isGroupStaffAdapter!!.itemList?.forEach { item ->
                        onIdCheck(item)
                    }
                } else {
                    isGroupStaffAdapter!!.deselectAll()
                    isGroupStaffAdapter!!.itemList?.forEach { item ->
                        onIdUnchecked(item)
                    }
                }
            }
        }
    }

    private fun tapVisibility(tabLayout: TabLayout) {

        if(isUserDetails!!.staff_role == Constant.isStaffRole){
            if(SELECTED_SCHOOL_MENU == SH_HOMEWORK){
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
                tabLayout.getTabAt(4)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.select()

               //show send button only

            }
            else if(SELECTED_SCHOOL_MENU == SH_ASSIGNMENT){
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
                tabLayout.getTabAt(4)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.select()

                //show send and specific student button
            }
            else {
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(4)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.select()

            }

        }
        else{
            if(SELECTED_SCHOOL_MENU == SH_HOMEWORK){
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
                tabLayout.getTabAt(4)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.select()


                //show send button only

            }
            else if(SELECTED_SCHOOL_MENU == SH_ASSIGNMENT){
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
                tabLayout.getTabAt(4)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.select()

                //show send and specific student button
            }
            else{
                if (isUserDetails!!.staff_details.size > 1) {
                    tabLayout.post {
                        tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                    }
                    tabLayout.getTabAt(1)?.select()
                    isSelectedType = 1
                    isGetAcademicYear()
                } else {
                    binding.textdesc.visibility = View.VISIBLE
                }
            }
        }
    }
    // Please don't delete by sathish
//    private fun isLoadSubjectData() {
//        binding.rlaSubject.visibility = View.VISIBLE
//    }

    private fun isLoadStaffData(data: List<NameAndIds>) {
        isGroupStaffAdapter = GroupStaffAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = isGroupStaffAdapter
        Constant.executeAfterDelay {
            isGroupStaffAdapter = GroupStaffAdapter(
                data, this@RecipientActivity, this, Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = isGroupStaffAdapter
        }
    }

    private fun isLoadGroupData(isGetGroupListData: List<NameAndIds>?) {
        isGroupStaffAdapter = GroupStaffAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = isGroupStaffAdapter
        Constant.executeAfterDelay {
            isGroupStaffAdapter = GroupStaffAdapter(
                isGetGroupListData, this@RecipientActivity, this, Constant.isShimmerViewDisable
            )
            binding.recyclerView.adapter = isGroupStaffAdapter
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
            binding.chAllSelect.visibility = View.VISIBLE
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
                    binding.lblSuibject.text = selectedSubject.first
                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnSpecificStudent -> {
                selectedIds = isSectionSelectedIds.map { it.id }.toMutableList()
                val intent = Intent(this@RecipientActivity, SpecificStudent::class.java)
                intent.putExtra("isAcademicYearId", isAcademicYearId)
                intent.putIntegerArrayListExtra("isSelectedId", ArrayList(selectedIds))
                startActivity(intent)
            }

            R.id.rlaAcademicYear -> {
                showAcademicDropdown(
                    binding.rlaAcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
                    isAcademicYearId = selectedYear.id
                    isCurrentAcademicYear = selectedYear.current_academic_year
                    binding.chAllSelect.isChecked = false

                    if (isSelectedType == 0) {

                    } else if (isSelectedType == 1) {
                        isGetStandardSection()
                    } else if (isSelectedType == 2) {
                        isGetStandardSection()
                    } else if (isSelectedType == 3) {
                        isGetGroupList()
                    } else if (isSelectedType == 4) {
                        isGetStaffList()
                    }
                }
            }


            R.id.rlaStandard -> {
                isSectionId.clear()
                isSectionSelectedIds.clear()
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
                    isTargetType = Constant.isSchool
                    isCircularType = Constant.school
//                    isTypeOfName = "School"
                    isUserDetails?.staff_details?.get(0)?.school_id?.let {
                        selectedIds.add(it.toInt())
                    }
                } else if (isSelectedType == 1) {
                    isTargetType = Constant.isStandard
                    isCircularType = Constant.standard
                    isTypeOfName = "Standard"
                    selectedIds = isStandardSelectedIds.map { it.id }.toMutableList()
                } else if (isSelectedType == 2) {
                    isTargetType = Constant.isSection
                    isCircularType = Constant.section
                    isTypeOfName = "Section"
                    selectedIds = isSectionSelectedIds.map { it.id }.toMutableList()
                } else if (isSelectedType == 3) {
                    selectedIds = isGroupSelectedIds.map { it.id }.toMutableList()
                    isTargetType = Constant.isGroup
                    isCircularType = Constant.group
                    isTypeOfName = "Groups"
                } else if (isSelectedType == 4) {
                    selectedIds = isGroupSelectedIds.map { it.id }.toMutableList()
                    isTypeOfName = "Staff"
                    isTargetType = Constant.isStaff
                    isCircularType = Constant.staff
                }

                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {
                    var isAcademicYearNote: String? = null
                    if (!isCurrentAcademicYear) {
                        isAcademicYearNote =
                            "NOTE : This message is addressed to student in " + binding.lblAcademicYear.text.toString() + " which is not the communication academic year. Do you want to proceed?"
                    } else {
                        isAcademicYearNote = "Are you sure want to send this message?"
                    }

                    if (Constant.isClickType == 3) {
                        showSendConfirmationDialog(
                            "Selected target : " + selectedIds.size.toString(), isAcademicYearNote
                        )
                    } else {
                        showSendConfirmationDialog(
                            "Selected target : " + selectedIds.size.toString(),
                            isAcademicYearNote.toString()
                        )
                    }
                } else {
                    Constant.showAlert("Alert!", "Select atleast one $isTypeOfName", this)
                }
            }
        }
    }

    private fun isGetGroupList() {
        appViewModel!!.isGetGroupList(isAccessToken!!, isAcademicYearId, this)
    }

//    private fun isGetSubjectList(isSectionId: String) {
//        appViewModel!!.isGetSubjectList(
//            isAccessToken!!,
//            isAcademicYearId,
//            isSectionId.toString(),
//            this
//        )
//    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun isGetStaffList() {
        appViewModel!!.isGetStaffList(
            isAccessToken!!, this
        )
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(isSelectTarget: String, isMessage: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)


        alertMessage.text = isMessage
        lblSelectTarget.text = isSelectTarget

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val isTextData = Constant.isTextSendingData
            if (Constant.isClickType == 3) {
                val jsonObject = ApiCallRequest.isSendText(
                    isAcademicYearId = isAcademicYearId,
                    schoolId = selectedIds,
                    message = isTextData!!.isTitle,
                    description = isTextData.isContent,
                    targetType = Constant.isSchool
                )
                appViewModel!!.isSendText(isAccessToken!!, jsonObject, this)
            } else {

                if (Constant.isVoiceType == 3) {
                    val isVoiceData = Constant.isVoiceSendingData
                    voiceSendApi(isVoiceData!!.isAwsUrl)
                } else {
                    isFileUploadInAws(
                        Constant.isVoiceFile!!, isStaffDetails!!.school_id, "audio"
                    )
                }
            }

        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(isMessage: String) {
        val isTextData = Constant.isTextSendingData
        AlertDialog.Builder(this)
            .setTitle("Confirmation").setMessage(isMessage)
            .setPositiveButton("Yes,Send") { dialog, _ ->

                if (Constant.isClickType == 3) {
                    val jsonObject = ApiCallRequest.isSendText(
                        isAcademicYearId = isAcademicYearId,
                        schoolId = selectedIds,
                        message = isTextData!!.isTitle,
                        description = isTextData.isContent,
                        targetType = Constant.isSchool
                    )
                    appViewModel!!.isSendText(isAccessToken!!, jsonObject, this)
                } else {

                    if (Constant.isVoiceType == 3) {
                        val isVoiceData = Constant.isVoiceSendingData
                        voiceSendApi(isVoiceData!!.isAwsUrl)
                    } else {
                        isFileUploadInAws(
                            Constant.isVoiceFile!!, isStaffDetails!!.school_id, "audio"
                        )
                    }
                }


            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onIdCheck(group: NameAndIds) {
        if (!isGroupSelectedIds.any { it.id == group.id }) {
            isGroupSelectedIds.add(group)
        }
        if (isSelectedType == 3) {
            binding.chAllSelect.isChecked = isGroupSelectedIds.size == isGetGroupListData?.size
        } else {
            binding.chAllSelect.isChecked = isGroupSelectedIds.size == isGetStaffListData?.size
        }
    }

    override fun onIdUnchecked(group: NameAndIds) {
        isGroupSelectedIds.removeAll { it.id == group.id }
        binding.chAllSelect.isChecked = false
    }

    override fun onIdCheck(isStandard: Standard) {
        if (!isStandardSelectedIds.any { it.id == isStandard.id }) {
            isStandardSelectedIds.add(isStandard)
        }
        binding.chAllSelect.isChecked = isStandardSelectedIds.size == isGetStandard?.size

    }

    override fun onIdUnchecked(isStandard: Standard) {
        isStandardSelectedIds.removeAll { it.id == isStandard.id }
        binding.chAllSelect.isChecked = false
    }

    override fun onIdCheck(data: Section) {
        if (!isSectionSelectedIds.any { it.id == data.id }) {
            isSectionSelectedIds.add(data)
        }
        // Please don't delete by sathish
//        val idString = isSectionSelectedIds.joinToString(",") { it.id.toString() }
//        isGetSubjectList(idString)
        binding.chAllSelect.isChecked = isSectionSelectedIds.size == isSection?.size

        if (isSelectedType == 2) {
            if (isSectionSelectedIds.size == 1) {
                binding.btnSpecificStudent.isEnabled = true
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_orange)
            } else {
                binding.btnSpecificStudent.isEnabled = false
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_gray)
            }
        }
    }

    override fun onIdUnchecked(data: Section) {
        isSectionSelectedIds.removeAll { it.id == data.id }
        binding.chAllSelect.isChecked = false
        if (isSelectedType == 2) {
            if (isSectionSelectedIds.size == 1) {
                binding.btnSpecificStudent.isEnabled = true
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_orange)
            } else {
                binding.btnSpecificStudent.isEnabled = false
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_gray)
            }
        }
        // Please don't delete by sathish

//        val idString = isSectionSelectedIds.joinToString(",") { it.id.toString() }
//        isGetSubjectList(idString)
    }

    private fun isFileUploadInAws(
        isFilePath: String, schoolId: String, isFileType: String?
    ) {
        val isCountryId = SharedPreference.getCountryId(this)
        isAwsUploadingPreSigned!!.getPreSignedUrl(Constant.isPickingFileExtension,
            isFilePath, schoolId, isFileType!!,
            this, isCountryId!!,
            true,
            false,
            object : UploadCallback {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onUploadSuccess(
                    response: String?,
                    isFileUploaded: String?
                ) {
                    voiceSendApi(isFileUploaded)
                    Log.d("isSuccessFullUpload", "isSuccessFullUpload")
                }

                override fun onUploadError(error: String?) {
                    TODO("Not yet implemented")
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun voiceSendApi(isFileUploadedUrl: String?) {

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
            selectedIds = isGroupSelectedIds.map { it.id }.toMutableList()
        } else if (isSelectedType == 4) {
            isTargetType = Constant.isSection
            isCircularType = Constant.section
        }

        val isVoiceData = Constant.isVoiceSendingData
        val jsonObject = ApiCallRequest.isVoiceSend(
            isAcademicYearId = isAcademicYearId,
            isFileUploaded = isFileUploadedUrl,
            isClickType = isVoiceData!!.isClickType,
            selectedDates = isVoiceData.selectedDates,
            isStartTimeText = isVoiceData.isStartTimeText,
            isEndTimeText = isVoiceData.isEndTimeText,
            title = isVoiceData.title,
            isEmergency = isVoiceData.isEmergency,
            isScheduleCall = isVoiceData.isScheduleCall,
            schoolId = selectedIds,
            targetType = isTargetType!!,
            circularType = isCircularType!!,
            fileName  = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
    }
}

