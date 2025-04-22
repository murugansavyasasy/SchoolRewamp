package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.Constant.SH_ABSENTEEISM_REPORT
import com.vs.schoolmessenger.Utils.Constant.SH_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.SH_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.SH_ATTENDANCE_MARKING
import com.vs.schoolmessenger.Utils.Constant.SH_COMMUNICATION
import com.vs.schoolmessenger.Utils.Constant.SH_DAILY_COLLECTION
import com.vs.schoolmessenger.Utils.Constant.SH_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SH_FEE_PENDING_REPORT
import com.vs.schoolmessenger.Utils.Constant.SH_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.SH_LESSON_PLAN
import com.vs.schoolmessenger.Utils.Constant.SH_MARK_GEOMETRIC_ATTENDANCE
import com.vs.schoolmessenger.Utils.Constant.SH_MESSAGES_FROM_MANAGEMENT
import com.vs.schoolmessenger.Utils.Constant.SH_NOTICE_BOARD
import com.vs.schoolmessenger.Utils.Constant.SH_ONLINE_MEETING
import com.vs.schoolmessenger.Utils.Constant.SH_PTM
import com.vs.schoolmessenger.Utils.Constant.SH_SCHEDULE_EXAM_TEST
import com.vs.schoolmessenger.Utils.Constant.SH_SCHOOL_STRENGTH
import com.vs.schoolmessenger.Utils.Constant.SH_STAFF_WISE_GEOMETRIC_ATTENDANCE_REPORT
import com.vs.schoolmessenger.Utils.Constant.SH_STUDENT_REPORT
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolListActivityBinding

class SchoolList : BaseActivity<SchoolListActivityBinding>(), SchoolListClickListener,
    View.OnClickListener {

    override fun getViewBinding(): SchoolListActivityBinding {
        return SchoolListActivityBinding.inflate(layoutInflater)
    }

    private val selectedSchoolIds = mutableListOf<String>()
    var isMultipleSchool = false
    private lateinit var mAdapter: SchoolListAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isUserDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.lblSendToMultipleSchool.setOnClickListener(this)
        binding.lblSelectReceipients.setOnClickListener(this)
        binding.lblSend.setOnClickListener(this)
        binding.rlaAcademicYear.setOnClickListener(this)


        if(SELECTED_SCHOOL_MENU  == SH_COMMUNICATION || SELECTED_SCHOOL_MENU == SH_NOTICE_BOARD || SELECTED_SCHOOL_MENU == SH_ATTACHMENTS || SELECTED_SCHOOL_MENU == SH_SCHEDULE_EXAM_TEST || SELECTED_SCHOOL_MENU == SH_EVENTS
            || SELECTED_SCHOOL_MENU == SH_ONLINE_MEETING) {
            isMultipleSchool  = false
            if (Constant.isEmergencyVoiceNoticeBoard!!) {
                binding.lnrTab.visibility = View.GONE
            } else {
                binding.lnrTab.visibility = View.VISIBLE
            }
        }
        else{
            isMultipleSchool  = true
            binding.lnrTab.visibility = View.GONE
        }

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        isUserDetails = SharedPreference.getUserDetails(this)
        isGetAcademicYear()

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val loader = rootView.findViewById<View>(R.id.loader_root)
            loader?.let { rootView.removeView(it) }

            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication,this)
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val loader = rootView.findViewById<View>(R.id.loader_root)
            loader?.let { rootView.removeView(it) }
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication,this)
            }
        }

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            if (response != null && response.status) {
                response.data.let { academicList ->
                    val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                    isAcademicYear = reorderedList
                    binding.lblAcademicYear.text = isAcademicYear!![0].year
                    isAcademicYearId = isAcademicYear!![0].id
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isLoadData()
    }

    private fun isLoadData() {
        mAdapter = SchoolListAdapter(
            isMultipleSchool, selectedSchoolIds, null, this, this, Constant.isShimmerViewShow
        )
        binding.recycleSchools.layoutManager = LinearLayoutManager(this)
        binding.recycleSchools.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter = SchoolListAdapter(
                isMultipleSchool,
                selectedSchoolIds,
                Constant.isStaffDetails,
                this,
                this,
                Constant.isShimmerViewDisable
            )
            binding.recycleSchools.adapter = mAdapter
        }
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblSelectReceipients -> {
                binding.lblSend.visibility= View.GONE
                binding.linearlayout.visibility= View.GONE
                isMultipleSchool = false
                isChangeBackRound(binding.lblSelectReceipients)
            }

            R.id.lblSendToMultipleSchool -> {
                binding.lblSend.visibility= View.VISIBLE
                binding.linearlayout.visibility= View.VISIBLE
                isMultipleSchool = true
                isChangeBackRound(binding.lblSendToMultipleSchool)
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
                }
            }

            R.id.lblSend -> {
                for (i in selectedSchoolIds.indices) {
                    Log.d("SelectedSchoolId", selectedSchoolIds[i].toString())
                }
                if (selectedSchoolIds.isNotEmpty()) {
                    if (Constant.isClickType == 3) {
                        showSendConfirmationDialog("Are you want send this text to entire school?")
                    } else {
                        showSendConfirmationDialog("Are you want send this voice to entire school?")
                    }
                } else {
                    Constant.showValidationAlertPopup(
                        "Please select at least one school to send the message.",
                        this
                    )
                }
            }
        }
    }


    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!, this
        )
    }

    private fun isChangeBackRound(
        lblSelectedTab: TextView
    ) {
        isLoadData()
        // Reset backgrounds and colors
        binding.lblSendToMultipleSchool.background = null
        binding.lblSelectReceipients.background = null

        lblSelectedTab.background = ContextCompat.getDrawable(this, R.drawable.white_radious)
        lblSelectedTab.setTextColor(
            ContextCompat.getColor(
                application, R.color.black
            )
        )
    }

    override fun onItemClick(data: StaffDetails) {

        if(SELECTED_SCHOOL_MENU == SH_COMMUNICATION || SELECTED_SCHOOL_MENU == SH_ATTACHMENTS || SELECTED_SCHOOL_MENU == SH_HOMEWORK || SELECTED_SCHOOL_MENU == SH_ASSIGNMENT || SELECTED_SCHOOL_MENU == SH_ONLINE_MEETING
            || SELECTED_SCHOOL_MENU == SH_EVENTS || SELECTED_SCHOOL_MENU == SH_SCHEDULE_EXAM_TEST) {
            val intent = Intent(this, RecipientActivity::class.java)
            SharedPreference.putStaffDetails(this, data)
            startActivity(intent)
        }
        else{

            if(SELECTED_SCHOOL_MENU == SH_ATTENDANCE_MARKING){
                //go to attendance marking screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_ABSENTEEISM_REPORT){
                //go to absenteeism report screen

            }
            else if(SELECTED_SCHOOL_MENU == SH_SCHOOL_STRENGTH){
                //go to school strength  screen
            }

            else if(SELECTED_SCHOOL_MENU == SH_MESSAGES_FROM_MANAGEMENT){
                //go to messages from management  screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_DAILY_COLLECTION){
                //go to daily collection screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_STUDENT_REPORT){
                //go to student report screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_LESSON_PLAN){
                //go to lesson plan screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_FEE_PENDING_REPORT){
                //go to fee pending report screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_MARK_GEOMETRIC_ATTENDANCE){
                //go to mark gio metric attendance screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_STAFF_WISE_GEOMETRIC_ATTENDANCE_REPORT){
                //go to staff wise gio metric attendanc report screen
            }
            else if(SELECTED_SCHOOL_MENU == SH_PTM){
                //go to ptm  screen
            }
        }
    }

    private fun isFileUploadInAws(
        isFilePath: String, schoolId: String, isFileType: String?
    ) {
        val isCountryId = SharedPreference.getCountryId(this)
        isAwsUploadingPreSigned!!.getPreSignedUrl(isFilePath, schoolId, isFileType!!,
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
            schoolId = selectedSchoolIds,
            targetType = Constant.isSchool,
            circularType = Constant.school,
            fileName = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(isMessage: String) {
        val isTextData = Constant.isTextSendingData

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val loaderView = LayoutInflater.from(this).inflate(R.layout.lottie_loader, rootView, false)

        AlertDialog.Builder(this)
            .setTitle("Send Confirmation!")
            .setMessage(isMessage)
            .setPositiveButton("Yes") { dialog, _ ->
                rootView.addView(loaderView)
                if (Constant.isClickType == 3) {
                    val jsonObject = ApiCallRequest.isSendText(
                        isAcademicYearId = isAcademicYearId,
                        schoolId = selectedSchoolIds,
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
}