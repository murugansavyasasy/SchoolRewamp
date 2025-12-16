package com.vs.schoolmessenger.CommonScreens.SelectRecipient

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupStaffAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SubjectLoadAdapter.SubjectLoadAdapter
import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudent
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentSendingData
import com.vs.schoolmessenger.School.Event.Model.EventDetails
import com.vs.schoolmessenger.School.Homework.SectionDetails
import com.vs.schoolmessenger.School.LSRW.Model.LsrwnewTaskSendingData
import com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz.SaveCreateExamQuizDetails
import com.vs.schoolmessenger.School.QuizExam.Model.QuizCheckLevel.GetCheckLevelData
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_COMMUNICATION
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.DimOverlayManager
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectRecipientBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File


class RecipientActivity : BaseActivity<SelectRecipientBinding>(), View.OnClickListener,
    SectionListClickListener, StandardListClickListener, GroupListClickListener,
    VimeoVideoUpload.UploadCompletionListener {

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
    var selectedLevelValue = 0
    private var isGroupStaffAdapter: GroupStaffAdapter? = null
    private var isAccessToken: String? = null
    private var isUserDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null
    private var selectedIds = mutableListOf<String>()
    var isSelectedType = 0
    var isAcademicYearId = -1
    private var isSubjectId: Int? = null
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isCurrentAcademicYear = true
    var isTargetType: Int? = null
    var isCircularType: String? = null
    var isIframe = ""
    var isFileSize = ""
    var isValidAcademicYear = false
    var isSelectedAcademicYear: String? = null
    private var appViewModel: App? = null
    val handler = Handler(Looper.getMainLooper())
    private lateinit var dimOverlayManager: DimOverlayManager
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isTotalSelectedItem = 0
    var isStandardId = ""
    var isClickedTab = 0
    private var isAssignmentData: AssignmentSendingData? = null


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()


        binding.rytSend.setOnClickListener(this)
        binding.btnSpecificStudent.setOnClickListener(this)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rytAcademicYear.setOnClickListener(this)
        binding.tapEntireSchool.setOnClickListener(this)
        binding.tapStandards.setOnClickListener(this)
        binding.tabSectionsStudent.setOnClickListener(this)
        binding.tabGroups.setOnClickListener(this)
        binding.tapStaffs.setOnClickListener(this)
        Constant.hideLoading(this)

        dimOverlayManager = DimOverlayManager(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isUserDetails = SharedPreference.getUserDetails(this)
        binding.toolbarLayout.lblParentToolBar.text = isStaffDetails!!.school_name
        isAssignmentData = intent.getParcelableExtra(Constant.assignment_data)

        if (isStaffDetails!!.school_name_regional != "") {
            binding.toolbarLayout.lblSchoolName.visibility = View.GONE
            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name_regional
        } else {
            binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        }

        isGetAcademicYear()

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isLoadAcademicYear(isAcademicYear)
                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year } == true
                isSelectedAcademicYear = isAcademicYear!![0].year
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
            //Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                if (response.status) {
                    isGetSubjectListData = response.data
                    if (isGetSubjectListData!!.isNotEmpty()) {
                        //  isSubjectId = isGetSubjectListData!!.first().id
                        binding.rytSubjectDropDown.visibility = View.VISIBLE
                        binding.subjectlabel.visibility = View.VISIBLE
                        isLoadSubject(isGetSubjectListData)
                    }
                }
            }
        }

        appViewModel!!.isGetCheckLevel?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    isLoadCheckLevelData(response.data)
                }
            }
        }

        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)

            if (response != null) {
                if (response.status) {
                    isGetStandard = response.data
                    if (isGetStandard!!.isNotEmpty()) {
                        isLoadStandard(isGetStandard)
                        binding.txtNoData.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.chAllSelect.visibility = View.VISIBLE
                        binding.grouplabel.visibility = View.VISIBLE
                        Log.d("isSelectedType", isSelectedType.toString())
                        if (isSelectedType != 1) {
                            binding.rytStandardDropDown.visibility = View.VISIBLE
                            binding.bottomLayout.visibility = View.VISIBLE
                            isSection = isGetStandard!!.get(0).sections
                            binding.nomessage.visibility = View.GONE
                            isLoadData(isSection)
                            binding.grouplabel.text = resources.getString(R.string.Standards)
                        } else {
                            isLoadTheStandardData(isGetStandard)
                            binding.bottomLayout.visibility = View.VISIBLE
                            binding.grouplabel.text = resources.getString(R.string.Standards)
                        }

                    } else {
                        binding.recyclerView.visibility = View.GONE
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.rytStandardDropDown.visibility = View.GONE
                        binding.chAllSelect.visibility = View.GONE
                        binding.grouplabel.visibility = View.GONE
                        binding.bottomLayout.visibility = View.GONE
                        binding.txtNoData.text = response.message
                        binding.nomessage.visibility = View.VISIBLE

                    }
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rytStandardDropDown.visibility = View.GONE
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

        appViewModel!!.sendevent?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            ProgressDialogHelper.dismiss()
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isCreateQuiz?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            ProgressDialogHelper.dismiss()
            if (response != null) {
                Log.d("Response", response.status.toString())
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isAssignmentSend?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)
                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_send_assignment)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                        addProperty(APIKeyNames.menu_id, SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)
                }
            }
        }


        appViewModel!!.isCreateQuiz?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.islsrwSkillCreate?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            ProgressDialogHelper.dismiss()
            if (response != null) {
                if (response.status) {
                    Constant.showDataValidation(
                        resources.getString(R.string.success),
                        response.message,
                        this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.Oops),
                        response.message,
                        this
                    )
                }
            } else {
                Constant.showDataValidation(
                    getString(R.string.Oops),
                    getString(R.string.something_went_wrong_please_try_again_later),
                    this
                )
            }
        }


        appViewModel!!.islsrwSkillSubmit?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            ProgressDialogHelper.dismiss()
            if (response != null) {
                if (response.status) {
                    Constant.showDataValidation(
                        resources.getString(R.string.success),
                        response.message,
                        this
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.Oops),
                        response.message,
                        this
                    )
                }
            } else {
                Constant.showDataValidation(
                    getString(R.string.Oops),
                    getString(R.string.something_went_wrong_please_try_again_later),
                    this
                )
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)

            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)

                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_send_voice)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                        addProperty(APIKeyNames.menu_id, SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)
                }
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)

                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_send_text)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                        addProperty(APIKeyNames.menu_id, SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)
                }
            }
        }

        appViewModel!!.isSendHomeWork?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null) {
                Constant.showTopAlertPopup(response.message, this)

                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_send_homework)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                        addProperty(APIKeyNames.menu_id, SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)
                }
            }
        }

        appViewModel!!.isAttachmentSend?.observe(this) { response ->
            Constant.hideLoading(this@RecipientActivity)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)

                if (response.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_send_attachment)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                        addProperty(APIKeyNames.menu_id, SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)
                }
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
        Log.d("Tap Visibility Check", "Tap Debug Check")
        if (isUserDetails!!.staff_role == Constant.isStaffRole) {
            when (SELECTED_MENU_ID) {
                M_HOMEWORK, Constant.M_QUIZ_EXAM -> {
                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)
                }

                M_ASSIGNMENT -> {

                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)

                    //show send and specific student button
                }

                M_LSRW -> {
                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)
                }

                else -> {

                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tapEntireSchool.visibility = View.GONE
                    binding.tapStandards.visibility = View.VISIBLE
                    binding.tabSectionsStudent.visibility = View.VISIBLE
                    binding.tabGroups.visibility = View.VISIBLE
                    binding.tapStaffs.visibility = View.GONE
                    changeTapBg(Constant.isStandard)

                }
            }
        } else {
            Log.d("SELECTED_SCHOOL_MENU", SELECTED_MENU_ID.toString())
            when (SELECTED_MENU_ID) {
                M_HOMEWORK, Constant.M_QUIZ_EXAM -> {
                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)
                    //show send button only

                }

                M_ASSIGNMENT -> {
                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)
                }

                M_LSRW -> {
                    binding.nomessage.visibility = View.GONE
                    binding.nomessageEntire.visibility = View.GONE
                    binding.tabLayout.visibility = View.GONE
                    changeTapBg(Constant.isSection)

                }

                M_SCHOOL_CLASS_EVENTS -> {
                    binding.textdesc.visibility = View.VISIBLE
                    binding.bottomLayout.visibility = View.VISIBLE
                    binding.nomessageEntire.visibility = View.VISIBLE
                    binding.tapEntireSchool.visibility = View.VISIBLE
                    binding.tapStandards.visibility = View.VISIBLE
                    binding.tabSectionsStudent.visibility = View.GONE
                    binding.tabGroups.visibility = View.VISIBLE
                    binding.tapStaffs.visibility = View.GONE
                    changeTapBg(Constant.isSchool)
                    isGetAcademicYear()
                }

                else -> {
                    binding.textdesc.visibility = View.VISIBLE
                    binding.bottomLayout.visibility = View.VISIBLE
                    binding.nomessageEntire.visibility = View.VISIBLE
                    binding.tapEntireSchool.visibility = View.VISIBLE
                    binding.tapStandards.visibility = View.VISIBLE
                    binding.tabSectionsStudent.visibility = View.VISIBLE
                    binding.tabGroups.visibility = View.VISIBLE
                    binding.tapStaffs.visibility = View.VISIBLE
                    changeTapBg(Constant.isSchool)
                    isGetAcademicYear()

                }
            }
        }
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

    private fun isLoadCheckLevelData(data: List<GetCheckLevelData>?) {
        if (data.isNullOrEmpty()) return

        val levelList = data.toMutableList()
        levelList.add(0, GetCheckLevelData(0))

        val displayList = levelList.map {
            if (it.level == 0) "Select Level" else "Level ${it.level}"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.isSpinnerLevel.adapter = adapter
        binding.isSpinnerLevel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedLevel = levelList[position]
                    Log.d("DropdownMenu", "Clicked Level: ${selectedLevel.level}")

                    selectedLevelValue = if (position != 0) {
                        selectedLevel.level
                    } else {
                        0
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        binding.isSpinnerLevel.setSelection(0)
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

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter

        var lastSelectedPosition = -1
        var isFirstLoad = true

        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (!isFirstLoad && lastSelectedPosition == position) return

                lastSelectedPosition = position
                adapter.selectedPosition = position
                binding.nomessage.visibility = View.GONE
                val selectedOption = isAcademicYear!![position]
                isSelectedAcademicYear = selectedOption.year
                isAcademicYearId = selectedOption.id
                isCurrentAcademicYear = selectedOption.current_academic_year

                Log.d(
                    "DropdownMenu",
                    "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                )

                // Reset selections
                isSectionSelectedIds.clear()
                isStandardSelectedIds.clear()
                isGroupSelectedIds.clear()
                binding.subjectlabel.visibility = View.GONE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.rytLevelDropDown.visibility = View.GONE
                selectedIds.clear()
                binding.btnSpecificStudent.isEnabled = false
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.bg_gray)
                binding.chAllSelect.isChecked = false

                when (isSelectedType) {
                    1, 2 -> {
                        isGetStandardSection()
                    }

                    3 -> {
                        isGetGroupList()
                    }

                    4 -> {
                        isGetStaffList()
                    }
                }

                isFirstLoad = false
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadSubject(isSubject: List<NameAndIds>?) {
        if (isSubject.isNullOrEmpty()) return

        val subjectList = isSubject.toMutableList()
//        subjectList.add(0, NameAndIds(0, "Get Subject", "", "", ""))

        val adapter = SubjectLoadAdapter(this, subjectList)
        binding.isSpinnerSubject.adapter = adapter

        binding.isSpinnerSubject.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    val selectedItem = subjectList[position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Subject: ID = ${selectedItem.id}, Name = ${selectedItem.name}"
                    )
                    isSubjectId = selectedItem.id

                    if (Constant.M_QUIZ_EXAM == SELECTED_MENU_ID) {
//                        if (position != 0) {
                        binding.rytLevelDropDown.visibility = View.VISIBLE
                        isCheckLevel()
//                        } else {
//                            binding.rytLevelDropDown.visibility = View.GONE
//                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        binding.isSpinnerSubject.setSelection(0)
    }


    private fun isLoadStandard(isStandard: List<Standard>?) {
        val adapter = StandardDropDownListAdapter(this, isStandard)
        binding.isSpinnerSection.adapter = adapter
        binding.chAllSelect.isChecked = false
        isSectionId.clear()
        isSectionSelectedIds.clear()
        binding.isSpinnerSection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    isStandard!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard!![position].id}, Year = ${isStandard[position].name}"
                    )

                    isStandardId = isStandard[position].id.toString()
                    isSection = isStandard[position].sections
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.chAllSelect.isChecked = false
                    isSectionId.clear()
                    isSectionSelectedIds.clear()
//                    Every time when we change the Class we need to disable the specfic student
                    binding.btnSpecificStudent.isEnabled = false
                    binding.btnSpecificStudent.background =
                        ContextCompat.getDrawable(this@RecipientActivity, R.drawable.bg_gray)
                    binding.chAllSelect.isChecked = false
                    isLoadData(isSection)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {

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
                intent.putExtra(Constant.lblAcademicYear, isSelectedAcademicYear)
                intent.putStringArrayListExtra(Constant.isSelectedId, ArrayList(selectedIds))
                if (SELECTED_MENU_ID == M_ASSIGNMENT) {
//                    val isAssignmentData = intent.getParcelableExtra<AssignmentSendingData>(Constant.assignment_data)
                    intent.putExtra(Constant.assignment_data, isAssignmentData)
                    intent.putExtra("subject_id", isSubjectId)
                }
                startActivity(intent)
            }

            R.id.rytSend -> {
                var isTypeOfName = ""

                Log.d("isSelectedType++", isSelectedType.toString())
                when (isSelectedType) {
                    0 -> {
                        isTargetType = Constant.isSchool
                        isCircularType = Constant.school
                        selectedIds.clear()
                        isTypeOfName = "School"
                        isStaffDetails!!.school_id.let {
                            selectedIds.add(it)
                        }
                    }

                    1 -> {
                        isTargetType = Constant.isStandard
                        isCircularType = Constant.standard
                        isTypeOfName = resources.getString(R.string.Standard)
                        selectedIds = isStandardSelectedIds.map { it.id.toString() }.toMutableList()
                    }

                    2 -> {
                        isTargetType = Constant.isSection
                        isCircularType = Constant.section
                        isTypeOfName = resources.getString(R.string.Section)
                        selectedIds = isSectionSelectedIds.map { it.id.toString() }.toMutableList()
                    }

                    3 -> {
                        selectedIds = isGroupSelectedIds.map { it.id.toString() }.toMutableList()
                        isTargetType = Constant.isGroup
                        isCircularType = Constant.group
                        isTypeOfName = resources.getString(R.string.Group)
                    }

                    4 -> {
                        selectedIds = isGroupSelectedIds.map { it.id.toString() }.toMutableList()
                        isTypeOfName = resources.getString(R.string.Staff)
                        isTargetType = Constant.isStaff
                        isCircularType = Constant.staff
                    }
                }

                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {
                    var isAcademicYearNote: String? = null
                    if (!isCurrentAcademicYear) {
                        isAcademicYearNote =
                            resources.getString(R.string.NOTE_message_addressed) + isSelectedAcademicYear + resources.getString(
                                R.string.which_communication_academic
                            )
                    } else {
                        isAcademicYearNote =
                            resources.getString(R.string.are_you_sure_want_to_send_this_message)
                    }

                    if (Constant.M_QUIZ_EXAM == SELECTED_MENU_ID || M_HOMEWORK == SELECTED_MENU_ID || M_LSRW == SELECTED_MENU_ID) {
                        if (isSubjectId == null) {
                            Constant.showValidationAlertPopup(
                                getString(R.string.alert),
                                getString(R.string.select_the_subject),
                                this
                            )
                        } else {
                            if (Constant.M_QUIZ_EXAM == SELECTED_MENU_ID) {
                                if (selectedLevelValue == 0) {
                                    Constant.showValidationAlertPopup(
                                        getString(R.string.alert),
                                        getString(R.string.select_the_level),
                                        this
                                    )
                                } else {
                                    showSendConfirmationDialog(
                                        resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + isTypeOfName + resources.getString(
                                            R.string._s
                                        ), isAcademicYearNote
                                    )

                                }
                            } else {
                                showSendConfirmationDialog(
                                    resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + isTypeOfName + resources.getString(
                                        R.string._s
                                    ), isAcademicYearNote
                                )
                            }

                        }

                    } else {

                        showSendConfirmationDialog(
                            resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + isTypeOfName + resources.getString(
                                R.string._s
                            ), isAcademicYearNote
                        )
                    }

                } else {
                    Constant.showValidationAlertPopup(
                        getString(
                            R.string.alert
                        ),
                        resources.getString(R.string.Please_select_leastone) + " " + isTypeOfName + " " + resources.getString(
                            R.string.send_message
                        ),
                        this
                    )
                }
            }

            R.id.tapEntireSchool -> {
                if (isClickedTab != Constant.isSchool) {
                    changeTapBg(Constant.isSchool)
                }
            }

            R.id.tapStandards -> {
                if (isClickedTab != Constant.isStandard) {
                    changeTapBg(Constant.isStandard)
                }
            }

            R.id.tabSectionsStudent -> {
                if (isClickedTab != Constant.isSection) {
                    changeTapBg(Constant.isSection)
                }
            }

            R.id.tabGroups -> {
                if (isClickedTab != Constant.isGroup) {
                    changeTapBg(Constant.isGroup)
                }
            }

            R.id.tapStaffs -> {
                if (isClickedTab != Constant.isStaff) {
                    changeTapBg(Constant.isStaff)
                }
            }
        }
    }


    private fun changeTapBg(type: Int) {

        isClickedTab = type

        when (type) {
            Constant.isSchool -> {
                isSelectedType = 0
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
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                binding.rytStandardDropDown.visibility = View.GONE
                binding.grouplabel.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.subjectlabel.visibility = View.GONE
                binding.textdesc.visibility = View.VISIBLE
                binding.bottomLayout.visibility = View.VISIBLE
                binding.btnSpecificStudent.visibility = View.GONE

            }

            Constant.isStandard -> {
                isSelectedType = 1
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
                binding.chAllSelect.visibility = View.GONE
                binding.chAllSelect.isChecked = false
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                isDropDown = false
                isGetStandardSection()
                binding.rytStandardDropDown.visibility = View.GONE
                binding.grouplabel.text = resources.getString(R.string.Standards)
                binding.grouplabel.visibility = View.VISIBLE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.subjectlabel.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.btnSpecificStudent.visibility = View.GONE

            }

            Constant.isSection -> {
                isSelectedType = 2
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

                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                isDropDown = true
                isGetStandardSection()
                binding.recyclerView.visibility = View.GONE
                binding.rytStandardDropDown.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.grouplabel.visibility = View.VISIBLE
                binding.grouplabel.text = resources.getString(R.string.Standards)

                binding.chAllSelect.visibility = View.GONE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.subjectlabel.visibility = View.GONE
                if (SELECTED_MENU_ID == M_COMMUNICATION || SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_ASSIGNMENT) {
                    binding.btnSpecificStudent.visibility = View.VISIBLE
                } else {
                    binding.btnSpecificStudent.visibility = View.GONE
                }
                binding.btnSpecificStudent.isEnabled = false
                binding.btnSpecificStudent.background =
                    ContextCompat.getDrawable(this@RecipientActivity, R.drawable.bg_gray)
            }

            Constant.isGroup -> {
                isSelectedType = 3
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
                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                binding.grouplabel.text = resources.getString(R.string.Groups)
                selectedIds.clear()
                binding.rytStandardDropDown.visibility = View.GONE
                binding.grouplabel.visibility = View.VISIBLE
                binding.lblCreatedOn.visibility = View.VISIBLE
                binding.btnSpecificStudent.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.subjectlabel.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                if (isAcademicYearId != -1) {
                    isGetGroupList()
                }
            }

            Constant.isStaff -> {
                isSelectedType = 4
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

                isGroupSelectedIds.clear()
                isStandardSelectedIds.clear()
                isSectionSelectedIds.clear()
                selectedIds.clear()
                isDropDown = false
                isGetStaffList()
                binding.rytStandardDropDown.visibility = View.GONE
                binding.grouplabel.text = resources.getString(R.string.Staff)
                binding.grouplabel.visibility = View.VISIBLE
                binding.rytSubjectDropDown.visibility = View.GONE
                binding.subjectlabel.visibility = View.GONE
                binding.chAllSelect.visibility = View.GONE
                binding.textdesc.visibility = View.GONE
                binding.bottomLayout.visibility = View.GONE
                binding.btnSpecificStudent.visibility = View.GONE
                Log.d("isDropDown", isDropDown.toString())
            }
        }
    }

    private fun isGetGroupList() {
        Constant.showLoading(this@RecipientActivity)
        appViewModel!!.isGetGroupList(isAccessToken!!, isAcademicYearId, this)
    }

    private fun isCheckLevel() {
        appViewModel!!.isGetCheckLevel(
            isAccessToken!!, isStandardId, isSubjectId!!.toString(),
            ""
        )
    }

    private fun isGetSubjectList(isSectionId: String) {
        // Constant.showLoading(this@RecipientActivity)
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


    fun isUploadFilesInServer(isFileType: String?) {

//        val needsProcessing = Constant.selectedFiles.isNotEmpty() || isVideoSelectedArrayList.any {
//            !it.path.contains("player.vimeo.com")
//        }
//        if (needsProcessing) {
//            ProgressDialogHelper.show(this)
        // }
        ProgressDialogHelper.show(this)
        ProgressDialogHelper.updateProgress(0)

        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_LSRW) {
            if (Constant.selectedFiles.isNotEmpty()) {
                Constant.selectedFiles.removeAt(0)
            }
            Log.d("UploadDebug", "Removed first file due to menu type: $SELECTED_MENU_ID")
        }

        isTotalSelectedItem = Constant.selectedFiles.size
        Log.d("UploadDebug", "Total selected items: $isTotalSelectedItem")

        isVideoSelectedArrayList.clear()
        Constant.isAwsUploadedFiles.clear()

        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.type == FileType.VIDEO) {
                isVideoSelectedArrayList.add(file)
                iterator.remove()
            }
        }
        val numNonVideoFiles = Constant.selectedFiles.size
        val numVideos = isVideoSelectedArrayList.size

        val videoSteps = 10
        var totalTasks = (numNonVideoFiles * 2) + (numVideos * videoSteps)

        if (totalTasks == 0 && numVideos > 0) {
            totalTasks = videoSteps
        }
        var completedTasks = 0

        fun updateProgress() {
            if (totalTasks > 0) {
                val progress = (completedTasks * 100) / totalTasks
                ProgressDialogHelper.updateProgress(progress)
            } else {
                ProgressDialogHelper.dismiss()
            }
        }


        // After separating videos
//        ProgressDialogHelper.updateProgress(10)
        Log.d("UploadDebug", "ProgressDialogHelper.updateProgress(10) called")

        when {
            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(
                isFileType,
                totalTasks,
                { completedTasks++; updateProgress() })

            isVideoSelectedArrayList.isNotEmpty() -> videoUploading(
                totalTasks,
                { completedTasks++; updateProgress() })
//            Constant.selectedFiles.isNotEmpty() -> {
//                Log.d("UploadDebug", "Uploading non-video files to AWS...")
//                isFileUploadInAws(isFileType)
//            }
//
//            isVideoSelectedArrayList.isNotEmpty() -> {
//                Log.d("UploadDebug", "Uploading video files...")
//                videoUploading()
//            }

            else -> {
                Log.d("UploadDebug", "No files to upload.")
            }
        }
    }

    private fun isFileUploadInAws(
        isFileType: String?,
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        // Do not clear here if already cleared in isUploadFilesInServer; assuming it's cleared once
        // Constant.isAwsUploadedFiles.clear()  // Commented out to avoid double clear

        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                // Incremental progress for pre-signed files
//                val progress =
//                    (Constant.isAwsUploadedFiles.size * 100 / isTotalSelectedItem).toInt()
//                        .coerceAtMost(100)
//                ProgressDialogHelper.updateProgress(progress)
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (Constant.selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
//                ProgressDialogHelper.updateProgress(100)
                ProgressDialogHelper.dismiss()
                when (SELECTED_MENU_ID) {
                    M_HOMEWORK -> isHomeWorkSend()
                    M_COMMUNICATION -> voiceSendApi()
                    M_ASSIGNMENT -> isAssignmentSend()
                    M_LSRW -> isLsrwSkillSend()

                }
            } else {
//                videoUploading()
                videoUploading(totalTasks, onTaskComplete)
            }
        } else {

            Constant.selectedFiles.size
            val outputDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
            outputDir.mkdirs()
            val newSelectedFiles = mutableListOf<FileItem>()
            Constant.compressImageFilesOnly(
                context = this,
                files = Constant.selectedFiles,
                outputDir = outputDir.absolutePath,
                format = Bitmap.CompressFormat.JPEG,
                quality = 80,
                maxWidth = 1280,
                maxHeight = 1280,
                onEachProcessed = { original, outputPath, success ->
                    if (success && outputPath != null) {
                        val compressedFile = File(outputPath)
                        val originalSizeKB = try {
                            if (original.path.startsWith("content://")) {
                                contentResolver.openFileDescriptor(
                                    Uri.parse(original.path), "r"
                                )?.statSize ?: 0
                            } else {
                                File(original.path).length()
                            }
                        } catch (e: Exception) {
                            0L
                        }

                        Log.d(
                            "Compressor",
                            "Compressed: $outputPath (${compressedFile.length() / 1024}KB), Original: ${originalSizeKB / 1024}KB"
                        )

                        newSelectedFiles.add(FileItem(path = outputPath, type = original.type))
                        // Incremental progress during compression (10% to 50%)
//                        val compressedCount = newSelectedFiles.size
//                        val progress =
//                            10 + ((compressedCount.toFloat() / numToCompress) * 40).toInt()
//                        ProgressDialogHelper.updateProgress(progress.coerceAtMost(50))
                    } else {
                        Log.e("Compressor", "Failed: ${original.path}")
                    }
                    onTaskComplete()
                },
                onComplete = {
                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(newSelectedFiles)
                    // Progress after compression (50%)
//                    ProgressDialogHelper.updateProgress(50)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = Constant.selectedFiles.size
                    for (i in Constant.selectedFiles.indices) {
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            Constant.selectedFiles[i].path,
                            isStaffDetails!!.school_id,
                            isFileType!!,
                            this@RecipientActivity,
                            isCountryId!!,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?, isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    Constant.isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = Constant.selectedFiles.getOrNull(i)?.type?.name
                                                ?: "UNKNOWN"
                                        )
                                    )
                                    // Incremental progress during upload
//                                    val progress =
//                                        (Constant.isAwsUploadedFiles.size * 100 / isTotalSelectedItem).toInt()
//                                            .coerceAtMost(100)
//                                    ProgressDialogHelper.updateProgress(progress)
                                    onTaskComplete()
                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
//                                        ProgressDialogHelper.updateProgress(100)
                                        ProgressDialogHelper.dismiss()
                                        when (SELECTED_MENU_ID) {
                                            M_HOMEWORK -> isHomeWorkSend()
                                            M_COMMUNICATION -> voiceSendApi()
                                            M_ATTACHMENTS -> attachmentSendApi()
                                            M_SCHOOL_CLASS_EVENTS -> eventsendapi()
                                            M_ASSIGNMENT -> isAssignmentSend()
                                            M_LSRW -> isLsrwSkillSend()
                                        }
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading(totalTasks, onTaskComplete)
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    Log.d("isUploadIssue", error.toString())
                                    // Optionally handle error, e.g., retry or dismiss
                                    onTaskComplete()
                                }
                            })
                    }

                    Log.d("Compressor", "All files compressed and uploaded.")
                })
        }
    }

    private fun videoUploading(
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("player.vimeo.com")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                // Incremental progress update for pre-processed videos
//                val progress =
//                    (Constant.isAwsUploadedFiles.size * 100 / isTotalSelectedItem).toInt()
//                        .coerceAtMost(100)
//                ProgressDialogHelper.updateProgress(progress)
                iterator.remove()
            }
        }

        if (isVideoSelectedArrayList.isEmpty()) {
//            ProgressDialogHelper.updateProgress(100)
            ProgressDialogHelper.dismiss()
            when (SELECTED_MENU_ID) {
                M_HOMEWORK -> {
                    isHomeWorkSend()
                }

                M_ATTACHMENTS -> {
                    attachmentSendApi()
                }

                M_SCHOOL_CLASS_EVENTS -> {
                    eventsendapi()
                }

                M_ASSIGNMENT -> {
                    isAssignmentSend()
                }

                M_LSRW -> {
                    isLsrwSkillSend()
                }
            }
        } else {
            for (i in isVideoSelectedArrayList.indices) {
                Thread {
                    for (x in 1..10) {
                        Thread.sleep(400)
                        runOnUiThread { onTaskComplete() }
                    }
                }.start()

                VimeoVideoUpload.uploadVideo(
                    this, "quiz", "quiz", isVideoSelectedArrayList[i].path, this
                )
            }
        }
    }


    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        runOnUiThread {
            if (success) {
                Log.d("link", link.toString())
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = link.toString(), isFileType = Constant.VIDEO
                    )
                )
                // Incremental progress update
//                val progress =
//                    (Constant.isAwsUploadedFiles.size * 100 / isTotalSelectedItem).toInt()
//                        .coerceAtMost(100)
//                ProgressDialogHelper.updateProgress(progress)

                if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
//                    ProgressDialogHelper.updateProgress(100)
                    ProgressDialogHelper.dismiss()
                    when (SELECTED_MENU_ID) {
                        M_HOMEWORK -> {
                            isHomeWorkSend()
                        }

                        M_ATTACHMENTS -> {
                            attachmentSendApi()
                        }

                        M_SCHOOL_CLASS_EVENTS -> {
                            eventsendapi()
                        }

                        M_ASSIGNMENT -> {
                            isAssignmentSend()
                        }

                        M_LSRW -> {
                            isLsrwSkillSend()
                        }
                    }
                }
            }
        }
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
            // Optionally handle failure, e.g., dismiss dialog or show error
            ProgressDialogHelper.dismiss()
        }
    }


    fun showSendConfirmationDialog(isSelectTarget: String, isMessage: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

        alertMessage.text = isMessage
        lblSelectTarget.text = isSelectTarget
        lblSelectTarget.visibility = if (isSelectTarget.isEmpty()) View.GONE else View.VISIBLE

        okButton.setOnClickListener {
            alertDialog.dismiss()

            when (SELECTED_MENU_ID) {
                M_HOMEWORK, M_ATTACHMENTS, M_SCHOOL_CLASS_EVENTS, M_ASSIGNMENT, M_LSRW -> {
                    if (Constant.selectedFiles.size != 1) {
                        isUploadFilesInServer("file")
                    } else {
                        when (SELECTED_MENU_ID) {
                            M_HOMEWORK -> isHomeWorkSend()
                            M_SCHOOL_CLASS_EVENTS -> eventsendapi()
                            M_ASSIGNMENT -> isAssignmentSend()
                            M_LSRW -> isLsrwSkillSend()
                        }
                    }
                }

                M_COMMUNICATION -> {
                    if (Constant.isCommunicationType == 3) {
                        Constant.isTextSendingData?.let { textData ->
                            val json = ApiCallRequest.isSendText(
                                isAcademicYearId,
                                selectedIds,
                                textData.isTitle,
                                textData.isContent,
                                isTargetType!!
                            )
                            appViewModel?.isSendText(isAccessToken!!, json, this)
                        }
                    } else {
                        isUploadFilesInServer("audio")
                    }
                }

                Constant.M_QUIZ_EXAM -> {
                    Constant.showLoading(this)
                    val isQuizData =
                        intent.getSerializableExtra(Constant.create_quiz_exam_data) as? SaveCreateExamQuizDetails
                    if (isQuizData != null) {
                        Log.d("isQuizData", isQuizData.title)

                        val jsonObject = JsonObject().apply {
                            addProperty("title", isQuizData.title)
                            addProperty("description", isQuizData.description)
                            addProperty("no_of_question", isQuizData.no_of_question.toInt())
                            addProperty("target_type", isTargetType)
                            addProperty("level", selectedLevelValue)
                            addProperty("level_flag", isQuizData.level_flag)
                            addProperty("subject_id", isSubjectId!!.toString())
                            addProperty("class_id", isStandardId)

                            val jsonArray = JsonArray()
                            selectedIds.forEach { id ->
                                jsonArray.add(id)
                            }
                            add("target_code", jsonArray)
                        }

                        Log.d("CreateQuizRequest", jsonObject.toString())
                        appViewModel!!.isCreateQuiz(isAccessToken!!, jsonObject)
                    }
                }
            }
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    fun eventsendapi() {

        val eventDetails = intent.getSerializableExtra(Constant.event_data) as? EventDetails
        if (eventDetails != null) {
            val jsonObject = ApiCallRequest.isSendEvent(
                title = eventDetails.txtTitle,
                content = eventDetails.txtDesc,
                venue = eventDetails.txtLocation,
                event_date = eventDetails.txtStartDate,
                event_time = eventDetails.txtStartTime,
                target_type = isTargetType,
                target_code = selectedIds,
                iframe = isIframe,
                fileSize = isFileSize,
                isSelectedCategory = eventDetails.isCategory
            )
            Log.d("Object", jsonObject.toString())
            appViewModel!!.sendevent(isAccessToken!!, jsonObject, this)

        } else {
            Log.e("RecepientEventList", "EventDetails not found in intent")
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
        if (SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_LSRW || SELECTED_MENU_ID == Constant.M_QUIZ_EXAM) {
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
        if (SELECTED_MENU_ID == M_HOMEWORK || SELECTED_MENU_ID == M_ASSIGNMENT || SELECTED_MENU_ID == M_LSRW || SELECTED_MENU_ID == Constant.M_QUIZ_EXAM) {
            isGetSubjectList(idString)
        }
    }

    fun attachmentSendApi() {
        val jsonObject = ApiCallRequest.isSendAttachment(
            isAcademicYearId = isAcademicYearId,
            selectedIds = selectedIds,
            title = Constant.isCommonTitle,
            description = Constant.isCommonDescription,
            targetType = isTargetType!!,
            iframe = isIframe,
            fileSize = isFileSize,
        )
        appViewModel!!.sendAttachment(isAccessToken!!, jsonObject, this)
    }

    fun isAssignmentSend() {
//        val isAssignmentData = intent.getParcelableExtra<AssignmentSendingData>(Constant.assignment_data)
        isAssignmentData?.let {
            val jsonObject = ApiCallRequest.isSendAssignment(
                targetType = isTargetType!!,
                iframe = isIframe,
                file_size = isFileSize,
                isAcademicYearId = isAcademicYearId,
                selectedIds = selectedIds,
                title = it.isTitle,
                description = it.isDescription,
                assignmentType = it.isAssignmentType,
                date = it.isDate,
                time = it.isTime,
                subjectId = isSubjectId!!,
            )
            appViewModel!!.isSendAssignment(isAccessToken!!, jsonObject, this)
        } ?: run {
            Constant.showValidationAlertPopup(
                getString(
                    R.string.alert
                ), "Assignment details is missing.", this
            )
        }
    }


    fun isLsrwSkillSend() {
        val isLsrwnewTaskSendingData =
            intent.getParcelableExtra<LsrwnewTaskSendingData>(Constant.lsrwskill_data)
        isLsrwnewTaskSendingData?.let {
            val jsonObject = ApiCallRequest.isSendLsrwSkill(
                targetType = isTargetType!!,
                iframe = isIframe,
                thumbnail = "",
                file_size = isFileSize,
                selectedIds = selectedIds,
                title = it.isTitle,
                description = it.isDescription,
                isLsrwType = it.isLsrwType,
                submission_date = it.submission_date,
                subjectId = isSubjectId!!
            )
            appViewModel!!.islsrwSkillCreate(isAccessToken!!, jsonObject, this)

        } ?: run {
            Constant.showValidationAlertPopup(
                getString(
                    R.string.alert
                ), "Task details is missing.", this
            )
        }
    }

    fun isHomeWorkSend() {
//        ProgressDialogHelper.updateProgress(100)
        ProgressDialogHelper.dismiss()
        val sectionDetails = intent.getParcelableExtra<SectionDetails>(Constant.section_data)
        sectionDetails?.let {
            val jsonObject = ApiCallRequest.isSendHomeWork(
                targetType = isTargetType!!,
                iframe = isIframe,
                file_size = isFileSize,
                isAcademicYearId = isAcademicYearId,
                selectedIds = selectedIds,
                title = it.title,
                description = it.description,
                subjectId = isSubjectId!!,
            )
            appViewModel!!.isSendHomeWork(isAccessToken!!, jsonObject, this)
        } ?: run {
            Constant.showValidationAlertPopup(
                getString(
                    R.string.alert
                ), resources.getString(R.string.Section_details_missing), this
            )
        }
    }

    fun voiceSendApi() {

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