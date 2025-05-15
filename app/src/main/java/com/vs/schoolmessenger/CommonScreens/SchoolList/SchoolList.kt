package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StaffWiseAttendanceReport.StaffWiseAttendanceReport
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ABSENTEES_REPORT
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_ATTENDANCE_MARKING
import com.vs.schoolmessenger.Utils.Constant.M_COMMUNICATION
import com.vs.schoolmessenger.Utils.Constant.M_DAILY_COLLECTION
import com.vs.schoolmessenger.Utils.Constant.M_EVENTS_HOLIDAYS
import com.vs.schoolmessenger.Utils.Constant.M_FEE_PENDING_REPORT
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_LESSON_PLAN
import com.vs.schoolmessenger.Utils.Constant.M_MARK_YOUR_ATTENDANCE
import com.vs.schoolmessenger.Utils.Constant.M_MESSAGES_FROM_MANAGEMENT
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_ONLINE_MEETING
import com.vs.schoolmessenger.Utils.Constant.M_PTM
import com.vs.schoolmessenger.Utils.Constant.M_SCHEDULE_EXAM_TEST
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_STRENGTH
import com.vs.schoolmessenger.Utils.Constant.M_STAFF_WISE_ATTENDANCE_REPORT
import com.vs.schoolmessenger.Utils.Constant.M_STUDENT_REPORT
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileItem
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
    private var isStaffData: StaffDetails? = null
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
        binding.rytSend.setOnClickListener(this)
        binding.rlaAcademicYear.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffData = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffData!!.access_token

        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        isUserDetails = SharedPreference.getUserDetails(this)

        if (SELECTED_SCHOOL_MENU == M_COMMUNICATION || SELECTED_SCHOOL_MENU == M_NOTICEBOARD || SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_SCHEDULE_EXAM_TEST || SELECTED_SCHOOL_MENU == M_EVENTS_HOLIDAYS || SELECTED_SCHOOL_MENU == M_ONLINE_MEETING) {
            isMultipleSchool = false
            if (Constant.isEmergencyVoiceNoticeBoard!!) {
                binding.lnrTab.visibility = View.GONE
            } else {
                binding.lnrTab.visibility = View.VISIBLE
            }
        } else if (SELECTED_SCHOOL_MENU == M_MARK_YOUR_ATTENDANCE || SELECTED_SCHOOL_MENU == M_STAFF_WISE_ATTENDANCE_REPORT || SELECTED_SCHOOL_MENU == M_STUDENT_REPORT || SELECTED_SCHOOL_MENU == M_SCHOOL_STRENGTH || SELECTED_SCHOOL_MENU == M_ABSENTEES_REPORT || SELECTED_SCHOOL_MENU == M_DAILY_COLLECTION || SELECTED_SCHOOL_MENU == M_FEE_PENDING_REPORT || SELECTED_SCHOOL_MENU == M_ATTENDANCE_MARKING) {
            isMultipleSchool = false
            binding.lnrTab.visibility = View.GONE
        } else {
            isMultipleSchool = true
            binding.lnrTab.visibility = View.GONE
        }

        isGetAcademicYear()

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication, this)
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication, this)
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
        binding.recycleSchools.layoutManager = LinearLayoutManager(this)
        mAdapter = SchoolListAdapter(
            isMultipleSchool,
            selectedSchoolIds,
            isUserDetails!!.staff_details,
            this,
            this,
            Constant.isShimmerViewDisable
        )
        binding.recycleSchools.adapter = mAdapter

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
                binding.rytSend.visibility = View.GONE
                binding.linearlayout.visibility = View.GONE
                isMultipleSchool = false
                isChangeBackRound(binding.lblSelectReceipients)
            }

            R.id.lblSendToMultipleSchool -> {
                binding.rytSend.visibility = View.VISIBLE
                binding.linearlayout.visibility = View.VISIBLE
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

            R.id.rytSend -> {
                for (i in selectedSchoolIds.indices) {
                    Log.d("SelectedSchoolId", selectedSchoolIds[i].toString())
                }
                if (selectedSchoolIds.isNotEmpty()) {
                    if (Constant.isClickType == 3) {
                        showConfirmationAlert(
                            resources.getString(R.string.selected_target_1) + selectedSchoolIds.size.toString(),
                            resources.getString(R.string.are_you_sure_want_to_send_this_message)
                        )
                    } else {
                        showConfirmationAlert(
                            resources.getString(R.string.selected_target_1) + selectedSchoolIds.size.toString(),
                            resources.getString(R.string.are_you_sure_want_to_send_this_message)
                        )
                    }
                } else {
                    Constant.showValidationAlertPopup(
                        resources.getString(R.string.Please_select_least), this
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
        Log.d("SELECTED_SCHOOL_MENU", SELECTED_SCHOOL_MENU.toString())
        SharedPreference.putStaffDetails(this, data)

        if (SELECTED_SCHOOL_MENU == M_COMMUNICATION || SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_HOMEWORK || SELECTED_SCHOOL_MENU == M_ASSIGNMENT || SELECTED_SCHOOL_MENU == M_ONLINE_MEETING || SELECTED_SCHOOL_MENU == M_EVENTS_HOLIDAYS || SELECTED_SCHOOL_MENU == M_SCHEDULE_EXAM_TEST) {
            val intent = Intent(this, RecipientActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            if (SELECTED_SCHOOL_MENU == M_ATTENDANCE_MARKING) {
                val intent = Intent(this, AttendanceMark::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_ABSENTEES_REPORT) {
                val intent = Intent(this, AbsenteesReport::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)

            } else if (SELECTED_SCHOOL_MENU == M_SCHOOL_STRENGTH) {
                val intent = Intent(this, SchoolStrength::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_MESSAGES_FROM_MANAGEMENT) {
                //go to messages from management  screen
            } else if (SELECTED_SCHOOL_MENU == M_DAILY_COLLECTION) {
                val intent = Intent(this, DailyCollection::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_STUDENT_REPORT) {
                val intent = Intent(this, StudentReport::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_LESSON_PLAN) {
                //go to lesson plan screen
            } else if (SELECTED_SCHOOL_MENU == M_FEE_PENDING_REPORT) {
                val intent = Intent(this, FeePendingReport::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_MARK_YOUR_ATTENDANCE) {
                val intent = Intent(this, MarkYourAttendance::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_STAFF_WISE_ATTENDANCE_REPORT) {
                val intent = Intent(this, StaffWiseAttendanceReport::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_PTM) {
                //go to ptm  screen
            }
        }
    }

    private fun isFileUploadInAws(
        isSelectedFiles: MutableList<FileItem>, schoolId: String, isFileType: String?
    ) {
        val isCountryId = SharedPreference.getCountryId(this)
        for (i in isSelectedFiles.indices) {
            isAwsUploadingPreSigned!!.getPreSignedUrl(
                isSelectedFiles[i].path, schoolId, isFileType!!,
                this, isCountryId!!,
                true,
                false,
                object : UploadCallback {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onUploadSuccess(
                        response: String?,
                        isFileUploaded: String?
                    ) {
                        Constant.isAwsUploadedFiles.add(
                            AwsUploadedFiles(
                                isFileUrl = isFileUploaded!!,
                                isFileType = isSelectedFiles[i].type.toString()
                            )
                        )
                        if (Constant.isAwsUploadedFiles.size == isSelectedFiles.size) {
                            voiceSendApi(isFileUploaded)
                        }
                        Log.d("isSuccessFullUpload", "isSuccessFullUpload")
                    }

                    override fun onUploadError(error: String?) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }

//    private fun isFileUploadInAws(
//        isFilePath: String, schoolId: String, isFileType: String?
//    ) {
//        val isCountryId = SharedPreference.getCountryId(this)
//        isAwsUploadingPreSigned!!.getPreSignedUrl(
//            isFilePath,
//            schoolId,
//            isFileType!!,
//            this,
//            isCountryId!!,
//            true,
//            false,
//            object : UploadCallback {
//                @RequiresApi(Build.VERSION_CODES.O)
//                override fun onUploadSuccess(
//                    response: String?, isFileUploaded: String?
//                ) {
//                    voiceSendApi(isFileUploaded)
//                    Log.d("isSuccessFullUpload", "isSuccessFullUpload")
//                }
//
//                override fun onUploadError(error: String?) {
//                    TODO("Not yet implemented")
//                }
//            })
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun voiceSendApi(isFileUploadedUrl: String?) {
        val isVoiceData = Constant.isVoiceSendingData
        val jsonObject = ApiCallRequest.isVoiceSend(
            isAcademicYearId = isAcademicYearId,
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

        AlertDialog.Builder(this).setTitle(resources.getString(R.string.Send_Confirmation))
            .setMessage(isMessage)
            .setPositiveButton(resources.getString(R.string.Yes)) { dialog, _ ->
                Constant.showLoading(this@SchoolList)

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
                            Constant.selectedFiles,
                            isStaffData!!.school_id,
                            "audio"
                        )
//                        isFileUploadInAws(
//                            Constant.selectedFiles.get(0).path!!, isStaffData!!.school_id, "audio"
//                        )
                    }
                }
            }.setNegativeButton(resources.getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showConfirmationAlert(isSelectTarget: String, isMessage: String) {
        val isTextData = Constant.isTextSendingData

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
        if (isSelectTarget.equals("")) {
            lblSelectTarget.visibility = View.GONE
        }
        okButton.setOnClickListener {
            alertDialog.dismiss()
            Constant.showLoading(this@SchoolList)

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
                        Constant.selectedFiles,
                        isStaffData!!.school_id,
                        "audio"
                    )
//                    isFileUploadInAws(
//                        Constant.selectedFiles.get(0).path!!, isStaffData!!.school_id, "audio"
//                    )
                }
            }
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}