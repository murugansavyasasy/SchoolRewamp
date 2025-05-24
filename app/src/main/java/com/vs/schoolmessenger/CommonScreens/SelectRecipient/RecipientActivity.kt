package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
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
import com.vs.schoolmessenger.School.Homework.SectionDetails
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileItem
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
    private var selectedIds = mutableListOf<String>()
    var isSelectedType = 0
    var isAcademicYearId = -1
    var isSubjectId = -1
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isCurrentAcademicYear = true
    var isTargetType: Int? = null
    var isCircularType: String? = null
    var isValidAcademicYear = false

    private var appViewModel: App? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rlaSubject.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)
        binding.rlaAcademicYear.setOnClickListener(this)
        binding.btnSpecificStudent.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.rytAcademicYear.setOnClickListener(this)

        binding.tapEntireSchool.setOnClickListener(this)
        binding.tapStandards.setOnClickListener(this)
        binding.tabSectionsStudent.setOnClickListener(this)
        binding.tabGroups.setOnClickListener(this)
        binding.tapStaffs.setOnClickListener(this)
        Constant.hideLoading(this)


        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isUserDetails = SharedPreference.getUserDetails(this)
        binding.lblSchoolName.text = isStaffDetails!!.school_name

        if (isStaffDetails!!.school_name_regional != "") {
            binding.lblSchoolRegionalName.visibility = View.GONE
            binding.lblSchoolRegionalName.text = isStaffDetails!!.school_name_regional
        } else {
            binding.lblSchoolRegionalName.visibility = View.GONE
        }

        isGetAcademicYear()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe  // Skip if same
                isAcademicYear = reorderedList

                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year == true } == true
                binding.lblAcademicYear.text = isAcademicYear!![0].year
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                if (isValidAcademicYear) {
                    binding.rytAcademicYear.visibility = View.GONE
                    binding.tabLayout.visibility = View.VISIBLE
                    if (isSelectedType != 0) {
                        isGetStandardSection()
                    }
                    tapVisibility()
                } else {
                    binding.lblSupportMail.paintFlags =
                        binding.lblSupportMail.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    binding.rytAcademicYear.visibility = View.VISIBLE
                    binding.tabLayout.visibility = View.GONE
                }
            }
        }

        appViewModel!!.isGetGroupList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)

            if (response != null) {
                isGetGroupListData = response.data
                if (isGetGroupListData!!.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.GONE
                    binding.chAllSelect.visibility = View.VISIBLE
                    binding.grouplabel.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                    binding.bottomLayout.visibility = View.VISIBLE
                    binding.lblCreatedOn.visibility = View.VISIBLE
                } else {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.txtNoData.text = response.message
                    binding.nomessage.visibility = View.VISIBLE
                    binding.bottomLayout.visibility = View.GONE
                    binding.lblCreatedOn.visibility = View.GONE
                }
                isLoadGroupData(isGetGroupListData)
            }
        }

        appViewModel!!.isGetSubjectList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                if (response.status) {
                    isGetSubjectListData = response.data
                    isSubjectId = isGetSubjectListData!!.first().id
                    isLoadSubjectData()
                }
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)

            if (response != null) {
                if (response.status) {
                    isGetStandard = response.data
                    if (isGetStandard!!.isNotEmpty()) {
                        binding.txtNoData.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.chAllSelect.visibility = View.VISIBLE
                        binding.grouplabel.visibility = View.VISIBLE
                        if (isSelectedType != 1) {
                            binding.rlaStandard.visibility = View.VISIBLE
                            binding.bottomLayout.visibility = View.VISIBLE
                            isSection = isGetStandard!!.get(0).sections
                            binding.lblStandard.text = isGetStandard!![0].name
                            binding.nomessage.visibility = View.GONE
                            isLoadData(isSection)
                            binding.grouplabel.text = resources.getString(R.string.Section)
                        } else {
                            isLoadTheStandardData(isGetStandard)
                            binding.bottomLayout.visibility = View.VISIBLE
                            binding.grouplabel.text = resources.getString(R.string.Standards)
                        }

                    } else {
                        binding.recyclerView.visibility = View.GONE
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.rlaStandard.visibility = View.GONE
                        binding.chAllSelect.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.bottomLayout.visibility = View.GONE
                        binding.txtNoData.text = response.message
                        binding.nomessage.visibility = View.VISIBLE

                    }
                } else {
                    Log.d("isComing", "isComing")
                    binding.recyclerView.visibility = View.GONE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rlaStandard.visibility = View.GONE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.bottomLayout.visibility = View.GONE
                    binding.txtNoData.text = response.message
                    binding.nomessage.visibility = View.VISIBLE
                }
            }
        }

        appViewModel!!.isGetStaffList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                isGetStaffListData = response.data

                if (isGetStaffListData!!.isNotEmpty()) {
                    binding.txtNoData.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.chAllSelect.visibility = View.VISIBLE
                    binding.grouplabel.visibility = View.VISIBLE
                    binding.bottomLayout.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                } else {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.chAllSelect.visibility = View.GONE
                    binding.grouplabel.visibility = View.GONE
                    binding.bottomLayout.visibility = View.GONE
                    binding.txtNoData.text = response.message
                    binding.nomessage.visibility = View.VISIBLE
                }
                isLoadStaffData(response.data)
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, Constant.isCommunication, this)
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication, this)
            }
        }

        appViewModel!!.isSendHomeWork?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)

            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, Constant.isHomeWork, this)
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
            } else if (isSelectedType == 4) {
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

    private fun tapVisibility() {
        if (isUserDetails!!.staff_role == Constant.isStaffRole) {
            if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.tapEntireSchool.visibility = View.GONE
                binding.tapStandards.visibility = View.GONE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabGroups.visibility = View.GONE
                binding.tapStaffs.visibility = View.GONE
                changeTapBg(Constant.isSection)

                //show send button only

            } else if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.tapEntireSchool.visibility = View.GONE
                binding.tapStandards.visibility = View.GONE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabGroups.visibility = View.GONE
                binding.tapStaffs.visibility = View.GONE
                changeTapBg(Constant.isSection)

                //show send and specific student button
            } else {

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.tapEntireSchool.visibility = View.GONE
                binding.tapStandards.visibility = View.VISIBLE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabGroups.visibility = View.VISIBLE
                binding.tapStaffs.visibility = View.GONE
                changeTapBg(Constant.isStandard)

            }

        } else {
            Log.d("SELECTED_SCHOOL_MENU", SELECTED_SCHOOL_MENU.toString())
            if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.tapEntireSchool.visibility = View.GONE
                binding.tapStandards.visibility = View.GONE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabLayout.visibility = View.GONE
                binding.tabGroups.visibility = View.GONE
                binding.tapStaffs.visibility = View.GONE
                changeTapBg(Constant.isSection)
                //show send button only

            } else if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {
                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.tapEntireSchool.visibility = View.GONE
                binding.tapStandards.visibility = View.GONE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabGroups.visibility = View.GONE
                binding.tapStaffs.visibility = View.GONE
                changeTapBg(Constant.isSection)

                //show send and specific student button
            } else {
                binding.textdesc.visibility = View.VISIBLE
                binding.bottomLayout.visibility = View.VISIBLE
                binding.nomessageEntire.visibility = View.VISIBLE

                binding.tapEntireSchool.visibility = View.VISIBLE
                binding.tapStandards.visibility = View.VISIBLE
                binding.tabSectionsStudent.visibility = View.VISIBLE
                binding.tabGroups.visibility = View.VISIBLE
                binding.tapStaffs.visibility = View.VISIBLE
                changeTapBg(Constant.isSchool)
                isSelectedType = 0
                isGetAcademicYear()

            }
        }
    }

    private fun isLoadSubjectData() {
        binding.rlaSubject.visibility = View.VISIBLE
    }

    private fun isLoadStaffData(data: List<NameAndIds>) {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        isGroupStaffAdapter = GroupStaffAdapter(
            false, data, this@RecipientActivity, this, Constant.isShimmerViewDisable
        )
        binding.recyclerView.adapter = isGroupStaffAdapter
    }

    private fun isLoadGroupData(isGetGroupListData: List<NameAndIds>?) {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        isGroupStaffAdapter = GroupStaffAdapter(
            true, isGetGroupListData, this@RecipientActivity, this, Constant.isShimmerViewDisable
        )
        binding.recyclerView.adapter = isGroupStaffAdapter

    }

    private fun isLoadData(isSection: List<Section>?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        isSectionAdapter = SectionListAdapter(
            isSection, this@RecipientActivity, this, Constant.isShimmerViewDisable
        )
        binding.recyclerView.adapter = isSectionAdapter
        binding.chAllSelect.visibility = View.VISIBLE

    }

    private fun isLoadTheStandardData(isGetStandard: List<Standard>?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        isStandardListAdapter = StandardListAdapter(
            isGetStandard, this, this, Constant.isShimmerViewDisable
        )
        binding.recyclerView.adapter = isStandardListAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaSubject -> {
                isDropDownLoadData(
                    binding.rlaSubject, this, isGetSubjectListData
                ) { selectedSubject ->
                    binding.lblSuibject.text = selectedSubject.first
                    isSubjectId = selectedSubject.second
                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rytAcademicYear -> {
                val sub = Constant.isMailTitle
                val body = Constant.isMailSend
                Constant.redirectToMail(this, binding.lblSupportMail.text.toString(), sub, body)
            }

            R.id.btnSpecificStudent -> {
                selectedIds = isSectionSelectedIds.map { it.id.toString() }.toMutableList()
                val intent = Intent(this@RecipientActivity, SpecificStudent::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(Constant.isAcademicYearId, isAcademicYearId)
                intent.putExtra(Constant.isCurrentAcademicYear, isCurrentAcademicYear)
                intent.putExtra(Constant.lblAcademicYear, binding.lblAcademicYear.text.toString())
                intent.putStringArrayListExtra(Constant.isSelectedId, ArrayList(selectedIds))
                startActivity(intent)
            }

            R.id.rlaAcademicYear -> {
                showAcademicDropdown(
                    binding.rlaAcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    isSectionSelectedIds.clear()
                    isStandardSelectedIds.clear()
                    isGroupSelectedIds.clear()
                    selectedIds.clear()
                    binding.btnSpecificStudent.isEnabled = false
                    binding.btnSpecificStudent.background =
                        ContextCompat.getDrawable(this, R.drawable.bg_gray)

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
                binding.chAllSelect.isChecked = false
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

            R.id.rytSend -> {
                var isTypeOfName = ""
                if (isSelectedType == 0) {
                    isTargetType = Constant.isSchool
                    isCircularType = Constant.school
                    selectedIds.clear()
                    isUserDetails?.staff_details?.get(0)?.school_id?.let {
                        selectedIds.add(it)
                    }
                } else if (isSelectedType == 1) {
                    isTargetType = Constant.isStandard
                    isCircularType = Constant.standard
                    isTypeOfName = resources.getString(R.string.Standard)
                    selectedIds = isStandardSelectedIds.map { it.id.toString() }.toMutableList()
                } else if (isSelectedType == 2) {
                    isTargetType = Constant.isSection
                    isCircularType = Constant.section
                    isTypeOfName = resources.getString(R.string.Section)
                    selectedIds = isSectionSelectedIds.map { it.id.toString() }.toMutableList()
                } else if (isSelectedType == 3) {
                    selectedIds = isGroupSelectedIds.map { it.id.toString() }.toMutableList()
                    isTargetType = Constant.isGroup
                    isCircularType = Constant.group
                    isTypeOfName = resources.getString(R.string.Group)
                } else if (isSelectedType == 4) {
                    selectedIds = isGroupSelectedIds.map { it.id.toString() }.toMutableList()
                    isTypeOfName = resources.getString(R.string.Staff)
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
                            resources.getString(R.string.NOTE_message_addressed) + binding.lblAcademicYear.text.toString() + resources.getString(
                                R.string.which_communication_academic
                            )
                    } else {
                        isAcademicYearNote =
                            resources.getString(R.string.are_you_sure_want_to_send_this_message)
                    }

                    if (isSelectedType == 0) {
                        showSendConfirmationDialog(
                            "", isAcademicYearNote
                        )
                    } else {
                        if (Constant.isCommunicationType == 3) {
                            showSendConfirmationDialog(
                                resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + isTypeOfName + resources.getString(
                                    R.string._s
                                ), isAcademicYearNote
                            )
                        } else {
                            showSendConfirmationDialog(
                                resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + isTypeOfName + resources.getString(
                                    R.string._s
                                ), isAcademicYearNote.toString()
                            )
                        }
                    }

                } else {
                    Constant.showValidationAlertPopup(
                        getString(
                            R.string.alert
                        ),
                        resources.getString(R.string.Please_select_leastone) + " " + isTypeOfName + " " + resources.getString(
                            R.string.send_message
                        ), this
                    )
                }
            }

            R.id.tapEntireSchool -> {
                changeTapBg(Constant.isSchool)
            }

            R.id.tapStandards -> {
                changeTapBg(Constant.isStandard)
            }

            R.id.tabSectionsStudent -> {
                changeTapBg(Constant.isSection)
            }

            R.id.tabGroups -> {
                changeTapBg(Constant.isGroup)
            }

            R.id.tapStaffs -> {
                changeTapBg(Constant.isStaff)
            }
        }
    }

    private fun changeTapBg(type: Int) {

        when (type) {
            Constant.isSchool -> {
                binding.tapEntireSchool.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.white_radious)
                binding.tapStandards.background = null
                binding.tabSectionsStudent.background = null
                binding.tabGroups.background = null
                binding.tapStaffs.background = null

                binding.nomessageEntire.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.lblCreatedOn.visibility = View.GONE
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
                binding.bottomLayout.visibility = View.VISIBLE
                binding.btnSpecificStudent.visibility = View.GONE

            }

            Constant.isStandard -> {
                binding.tapEntireSchool.background = null
                binding.tapStandards.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.white_radious)
                binding.tabSectionsStudent.background = null
                binding.tabGroups.background = null
                binding.tapStaffs.background = null

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.lblCreatedOn.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
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
                binding.grouplabel.text = resources.getString(R.string.Standards)
                binding.grouplabel.visibility = View.VISIBLE
                binding.rlaSubject.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.btnSpecificStudent.visibility = View.GONE

            }

            Constant.isSection -> {
                binding.tapEntireSchool.background = null
                binding.tapStandards.background = null
                binding.tabSectionsStudent.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.white_radious)
                binding.tabGroups.background = null
                binding.tapStaffs.background = null

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.lblCreatedOn.visibility = View.GONE
                binding.chAllSelect.isChecked = false
                isSelectedType = 2
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                isDropDown = true
                isGetStandardSection()
                binding.recyclerView.visibility = View.GONE
                binding.rlaStandard.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.grouplabel.visibility = View.VISIBLE
                binding.grouplabel.text = resources.getString(R.string.Section)

                binding.chAllSelect.visibility = View.GONE
                binding.rlaSubject.visibility = View.GONE
                if (SELECTED_SCHOOL_MENU == Constant.M_COMMUNICATION) {
                    binding.btnSpecificStudent.visibility = View.VISIBLE
                } else {
                    binding.btnSpecificStudent.visibility = View.GONE
                }
                binding.btnSpecificStudent.isEnabled = false
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.bg_gray)

            }

            Constant.isGroup -> {
                binding.tapEntireSchool.background = null
                binding.tapStandards.background = null
                binding.tabSectionsStudent.background = null
                binding.tabGroups.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.white_radious)
                binding.tapStaffs.background = null

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.chAllSelect.visibility = View.GONE
                binding.chAllSelect.isChecked = false
                isSelectedType = 3
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                binding.grouplabel.text = resources.getString(R.string.Groups)
                selectedIds.clear()
                binding.rlaStandard.visibility = View.GONE
                binding.grouplabel.visibility = View.VISIBLE
                binding.lblCreatedOn.visibility = View.VISIBLE
                binding.btnSpecificStudent.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.rlaSubject.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                if (isAcademicYearId != -1) {
                    isGetGroupList()
                }

            }

            Constant.isStaff -> {
                binding.tapEntireSchool.background = null
                binding.tapStandards.background = null
                binding.tabSectionsStudent.background = null
                binding.tabGroups.background = null
                binding.tapStaffs.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.white_radious)

                binding.nomessage.visibility = View.GONE
                binding.nomessageEntire.visibility = View.GONE
                binding.lblCreatedOn.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.chAllSelect.isChecked = false
                isSelectedType = 4
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                isDropDown = false
                isGetStaffList()
                binding.rlaStandard.visibility = View.GONE
                binding.grouplabel.text = resources.getString(R.string.Staff)
                binding.grouplabel.visibility = View.VISIBLE
                binding.rlaSubject.visibility = View.GONE
                binding.chAllSelect.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.btnSpecificStudent.visibility = View.GONE
                Log.d("isDropDown", isDropDown.toString())
            }
        }

    }

    private fun isGetGroupList() {
        appViewModel!!.isGetGroupList(isAccessToken!!, isAcademicYearId, this)
    }

    private fun isGetSubjectList(isSectionId: String) {
        appViewModel!!.isGetSubjectList(
            isAccessToken!!, isAcademicYearId, isSectionId.toString(), this
        )
    }

    private fun isGetStandardSection() {
        Constant.showLoading(this@RecipientActivity)
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    private fun isGetStaffList() {
        Constant.showLoading(this@RecipientActivity)
        appViewModel!!.isGetStaffList(
            isAccessToken!!, this
        )
    }

    private fun isGetAcademicYear() {
        Constant.showLoading(this@RecipientActivity)
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
        if (isSelectTarget == "") {
            lblSelectTarget.visibility = View.GONE
        }
        okButton.setOnClickListener {
            alertDialog.dismiss()
            Constant.showLoading(this@RecipientActivity)


            if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                if (Constant.selectedFiles.size > 0) {
                    isFileUploadInAws(
                        Constant.selectedFiles, isStaffDetails!!.school_id, "file"
                    )
                } else {
                    isHomeWorkSend()
                }

            } else if (SELECTED_SCHOOL_MENU == Constant.M_COMMUNICATION) {
                val isTextData = Constant.isTextSendingData
                if (Constant.isCommunicationType == 3) {
                    val jsonObject = ApiCallRequest.isSendText(
                        isAcademicYearId = isAcademicYearId,
                        schoolId = selectedIds,
                        message = isTextData!!.isTitle,
                        description = isTextData.isContent,
                        targetType = isTargetType!!
                    )
                    appViewModel!!.isSendText(isAccessToken!!, jsonObject, this)

                } else {
                    if (Constant.isVoiceType == 3) {
                        val isVoiceData = Constant.isVoiceSendingData
                        voiceSendApi(isVoiceData!!.isAwsUrl)
                    } else {
                        isFileUploadInAws(
                            Constant.selectedFiles, isStaffDetails!!.school_id, "audio"
                        )
                    }

                }
            }
//            else if () {
//
//            }

        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

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

        val idString = isSectionSelectedIds.joinToString(",") { it.id.toString() }
        if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
            isGetSubjectList(idString)
        }
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

        val idString = isSectionSelectedIds.joinToString(",") { it.id.toString() }
        if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
            isGetSubjectList(idString)
        }
    }

    private fun isFileUploadInAws(
        isSelectedFiles: MutableList<FileItem>, schoolId: String, isFileType: String?
    ) {
        Constant.isAwsUploadedFiles.clear()
        val isSelectedFileListSize = Constant.selectedFiles.size
        val iterator = Constant.selectedFiles.iterator()

        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path,
                        isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        Log.d("isSelectedFiles", isSelectedFiles.size.toString())
        for (i in isSelectedFiles.indices) {
            isAwsUploadingPreSigned!!.getPreSignedUrl(
                isSelectedFiles[i].path.toString(),
                schoolId,
                isFileType!!,
                this,
                isCountryId!!,
                true,
                false,
                object : UploadCallback {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onUploadSuccess(
                        response: String?, isFileUploaded: String?
                    ) {
                        Constant.isAwsUploadedFiles.add(
                            AwsUploadedFiles(
                                isFileUrl = isFileUploaded!!,
                                isFileType = isSelectedFiles[i].type.toString()
                            )
                        )
                        if (Constant.isAwsUploadedFiles.size == isSelectedFileListSize) {
                            if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                                isHomeWorkSend()
                            } else if (SELECTED_SCHOOL_MENU == Constant.M_COMMUNICATION) {
                                voiceSendApi(isFileUploaded)
                            }
                        } else {
                            Log.d("isFileNotMatching", "isFileNotMatching")
                        }
                        Log.d("isSuccessFullUpload", "isSuccessFullUpload")
                    }
                    override fun onUploadError(error: String?) {

                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isHomeWorkSend() {
        val sectionDetails = intent.getParcelableExtra<SectionDetails>(Constant.section_data)
        sectionDetails?.let {
            val jsonObject = ApiCallRequest.isSendHomeWork(
                isAcademicYearId = isAcademicYearId,
                selectedIds = selectedIds,
                title = it.title,
                description = it.description,
                subjectId = isSubjectId!!,
            )
            appViewModel!!.isSendHomeWork(isAccessToken!!, jsonObject, this)
        } ?: run {
            Constant.showValidationAlertPopup( getString(
                R.string.alert
            ),
                resources.getString(R.string.Section_details_missing), this
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun voiceSendApi(isFileUploadedUrl: String?) {

        val isVoiceData = Constant.isVoiceSendingData
        val jsonObject = ApiCallRequest.isVoiceSend(
            isAcademicYearId = isAcademicYearId,
            isCommunicationType = isVoiceData!!.isCommunicationType,
            selectedDates = isVoiceData.selectedDates,
            isStartTimeText = isVoiceData.isStartTimeText,
            isEndTimeText = isVoiceData.isEndTimeText,
            title = isVoiceData.title,
            isEmergency = isVoiceData.isEmergency,
            isScheduleCall = isVoiceData.isScheduleCall,
            schoolId = selectedIds,
            targetType = isTargetType!!,
            circularType = isCircularType!!,
            fileName = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
    }
}

