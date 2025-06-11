package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
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
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeBoardDetails
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
import com.vs.schoolmessenger.Utils.Constant.M_FEE_PENDING_REPORT
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_LESSON_PLAN
import com.vs.schoolmessenger.Utils.Constant.M_MARK_YOUR_ATTENDANCE
import com.vs.schoolmessenger.Utils.Constant.M_MESSAGES_FROM_MANAGEMENT
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_ONLINE_MEETING
import com.vs.schoolmessenger.Utils.Constant.M_PTM
import com.vs.schoolmessenger.Utils.Constant.M_SCHEDULE_EXAM_TEST
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_STRENGTH
import com.vs.schoolmessenger.Utils.Constant.M_STAFF_WISE_ATTENDANCE_REPORT
import com.vs.schoolmessenger.Utils.Constant.M_STUDENT_REPORT
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolListActivityBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload

class SchoolList : BaseActivity<SchoolListActivityBinding>(), SchoolListClickListener,
    View.OnClickListener, VimeoVideoUpload.UploadCompletionListener {

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
    var isTargetType: Int? = null
    var isIframe = ""
    var isFileSize = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.lblSendToMultipleSchool.setOnClickListener(this)
        binding.lblSelectReceipients.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffData = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffData!!.access_token

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        Constant.hideLoading(this)
        isUserDetails = SharedPreference.getUserDetails(this)

        if (SELECTED_SCHOOL_MENU == M_COMMUNICATION || SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_SCHEDULE_EXAM_TEST  || SELECTED_SCHOOL_MENU == M_ONLINE_MEETING) {
            isMultipleSchool = false
            if (Constant.isEmergencyVoiceNoticeBoard!!) {
                binding.lnrTab.visibility = View.GONE
            } else {
                binding.lnrTab.visibility = View.VISIBLE
            }
        } else if (SELECTED_SCHOOL_MENU == M_MARK_YOUR_ATTENDANCE || SELECTED_SCHOOL_MENU == M_STAFF_WISE_ATTENDANCE_REPORT || SELECTED_SCHOOL_MENU == M_STUDENT_REPORT || SELECTED_SCHOOL_MENU == M_SCHOOL_STRENGTH || SELECTED_SCHOOL_MENU == M_ABSENTEES_REPORT || SELECTED_SCHOOL_MENU == M_DAILY_COLLECTION || SELECTED_SCHOOL_MENU == M_FEE_PENDING_REPORT || SELECTED_SCHOOL_MENU == M_ATTENDANCE_MARKING || SELECTED_SCHOOL_MENU == M_HOMEWORK || SELECTED_SCHOOL_MENU == M_SCHOOL_CLASS_EVENTS) {
            isMultipleSchool = false
            binding.lnrTab.visibility = View.GONE
        } else if (SELECTED_SCHOOL_MENU == M_NOTICEBOARD) {
            isMultipleSchool = true
            binding.lnrTab.visibility = View.GONE
            binding.rytSend.visibility = View.VISIBLE
            binding.sendOnlyLayout.visibility = View.VISIBLE

        } else {
            isMultipleSchool = true
            binding.lnrTab.visibility = View.GONE
        }

        isGetAcademicYear()

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }
        appViewModel!!.isAttachmentSend?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.sendnotice?.observe(this) { response ->
            Constant.hideLoading(this@SchoolList)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            if (response != null && response.status) {
                response.data.let { academicList ->
                    val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                    isAcademicYear = reorderedList
                    isLoadAcademicYear(isAcademicYear)
                    isAcademicYearId = isAcademicYear!![0].id
                }
            }
        }

        binding.radioGroupSendTo.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as? RadioButton
                radioButton?.background = null
            }

            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            selectedRadioButton?.setBackgroundResource(R.drawable.radio_selected_bg) // Set selected background
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

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter

        binding.isSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isAcademicYear!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${isAcademicYear[position].id}, Year = ${isAcademicYear[position].year}, Current = ${isAcademicYear[position].current_academic_year}"
                    )
                    isAcademicYearId = isAcademicYear[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
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

            R.id.rytSend -> {
                for (i in selectedSchoolIds.indices) {
                    Log.d("SelectedSchoolId", selectedSchoolIds[i].toString())
                }
                if (selectedSchoolIds.isNotEmpty()) {
                    if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                        if (Constant.isCommunicationType == 3) {
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
                    } else if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS) {
                        showConfirmationAlert(
                            resources.getString(R.string.selected_target_1) + selectedSchoolIds.size.toString(),
                            resources.getString(R.string.are_you_sure_want_to_send_this_attachment)
                        )
                    } else if (SELECTED_SCHOOL_MENU == M_NOTICEBOARD) {
                        val selectedRadioId = binding.radioGroupSendTo.checkedRadioButtonId

                        if (selectedRadioId == -1) {
                            Toast.makeText(
                                this,
                                "Please select a recipient (All / Staff / Student)",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        showConfirmationAlert(
                            resources.getString(R.string.selected_target_1) + selectedSchoolIds.size.toString(),
                            resources.getString(R.string.are_you_sure_want_to_send_this_attachment)
                        )
                    }
                } else {
                    Constant.showValidationAlertPopup(
                        getString(
                            R.string.alert
                        ),
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

        if (SELECTED_SCHOOL_MENU == M_COMMUNICATION || SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_ASSIGNMENT || SELECTED_SCHOOL_MENU == M_ONLINE_MEETING  || SELECTED_SCHOOL_MENU == M_SCHEDULE_EXAM_TEST) {
            val intent = Intent(this, RecipientActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            if (SELECTED_SCHOOL_MENU == M_ATTENDANCE_MARKING) {
                val intent = Intent(this, AttendanceMark::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                val intent = Intent(this, HomeWork::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_SCHOOL_CLASS_EVENTS) {
                val intent = Intent(this, CreateEvent::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }  else if (SELECTED_SCHOOL_MENU == M_ABSENTEES_REPORT) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isFileUploadInAws(
        schoolId: String, isFileType: String?
    ) {
        Constant.isAwsUploadedFiles.clear()
        val isSelectedFileListSize = Constant.selectedFiles.size
        val iterator = Constant.selectedFiles.iterator()

        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        Log.d("isSelectedFiles", Constant.selectedFiles.size.toString())
        if (Constant.selectedFiles.isEmpty()) {
            if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                voiceSendApi()
            }
        } else {
            for (i in Constant.selectedFiles.indices) {
            isAwsUploadingPreSigned!!.getPreSignedUrl(
                Constant.selectedFiles[i].path.toString(),
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
                                isFileType = Constant.selectedFiles[i].type.toString()
                            )
                        )
                        if (Constant.isAwsUploadedFiles.size == isSelectedFileListSize) {
                            if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS) {
                                attachmentSendApi()
                            } else if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                                voiceSendApi()
                            } else if (SELECTED_SCHOOL_MENU == M_NOTICEBOARD) {
                                noticeboardsendapi()
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
    }


    @RequiresApi(Build.VERSION_CODES.O)
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
            schoolId = selectedSchoolIds,
            targetType = Constant.isSchool,
            circularType = Constant.school,
            fileName = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun attachmentSendApi() {
        val jsonObject = ApiCallRequest.isSendAttachment(
            isAcademicYearId = isAcademicYearId,
            selectedIds = selectedSchoolIds,
            title = Constant.isCommonTitle,
            description = Constant.isCommonDescription,
            targetType = Constant.isSchool,
            iframe = isIframe,
            fileSize = isFileSize,
        )
        appViewModel!!.sendAttachment(isAccessToken!!, jsonObject, this)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun noticeboardsendapi() {
        val selectedRadioId = binding.radioGroupSendTo.checkedRadioButtonId
        var intendedFor = ""

        if (selectedRadioId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioId)
            intendedFor = selectedRadioButton.text.toString().lowercase() // Force lowercase
            Log.d("noticeboardsendapi", "Selected intended_for: $intendedFor")
        } else {
            Log.d("noticeboardsendapi", "No option selected in radioGroupSendTo")
        }

        val noticeDetails = intent.getSerializableExtra(Constant.notice_data) as? NoticeBoardDetails
        if (noticeDetails != null) {
            val jsonObject = ApiCallRequest.isSendNotice(
                title = noticeDetails.title,
                description = noticeDetails.description,
                startDate = noticeDetails.txtStartDate,
                endDate = noticeDetails.txtEndDate,
                target_code = selectedSchoolIds,
                intended_for = intendedFor,
                iframe = isIframe,
                fileSize = isFileSize,
            )
            Log.d("SchoolList", "Notice details received and jsonObject created: $jsonObject")
            Log.d("Object", jsonObject.toString())
            appViewModel!!.sendnotice(isAccessToken!!, jsonObject, this)

        } else {
            Log.e("SchoolList", "NoticeBoardDetails not found in intent")
        }
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

            if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                if (Constant.isCommunicationType == 3) {
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
                        voiceSendApi()
                    } else {
                        isFileUploadInAws(
                            isStaffData!!.school_id,
                            "files"
                        )
                    }
                }
            } else if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS) {

                if (Constant.selectedFiles.isNotEmpty()) {
                    val videoFiles = Constant.selectedFiles.filter { it.type == FileType.VIDEO }
                    if (videoFiles.isNotEmpty()) {
                        videoSending()
                    } else {
                        isFileUploadInAws(
                            isStaffData!!.school_id, "audio"
                        )
                    }
                }

            } else if (SELECTED_SCHOOL_MENU == M_NOTICEBOARD) {
                if (Constant.selectedFiles.isNotEmpty()) {
                    val videoFiles = Constant.selectedFiles.filter { it.type == FileType.VIDEO }
                    if (videoFiles.isNotEmpty()) {
                        videoSending()
                    } else {
                        isFileUploadInAws(
                            isStaffData!!.school_id, "audio"
                        )
                    }
                } else {
                    noticeboardsendapi()
                }
            }
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    private fun videoSending() {
        VimeoVideoUpload.uploadVideo(
            this@SchoolList,
            "quiz", "quiz", Constant.selectedFiles[0].path,
            this@SchoolList
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUploadComplete(success: Boolean, iframe: String?, link: String?) {
        runOnUiThread {
            Log.d("Vimeo_Video_upload", success.toString())
            Log.d("VimeoIframe", iframe.toString())
            Log.d("link", link.toString())

            isIframe = extractVimeoUrlFromIframe(iframe.toString()).toString()
            isFileSize = Constant.getFileSizeInMB(Constant.selectedFiles[0].path)

            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )
            attachmentSendApi()
        }
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }
    override fun onProgressUpdate(percent: Int) {
        runOnUiThread {
            Log.d("VimeoUploadProgress", "Progress: $percent%")
        }
    }

    fun extractVimeoUrlFromIframe(iframeHtml: String): String? {
        val regex = Regex("""<iframe[^>]+src="([^"]+)"""")
        val match = regex.find(iframeHtml)
        return match?.groups?.get(1)?.value
    }
}