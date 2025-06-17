package com.vs.schoolmessenger.CommonScreens.SpecificStudentData

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_COMMUNICATION
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SpecificStudentBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload

class SpecificStudent : BaseActivity<SpecificStudentBinding>(), SpecificStudentSelectClickListener,
    View.OnClickListener, VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): SpecificStudentBinding {
        return SpecificStudentBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var selectedIds = mutableListOf<String>()
    lateinit var mAdapter: SpecificStudentAdapter
    val isSpecificStudent = mutableListOf<NameAndIds>()
    var isStudentData: List<NameAndIds>? = null
    private var isStaffDetails: StaffDetails? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = false
    var isAcademicYear: String? = null
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    private var isStudentList: List<NameAndIds> = listOf()
    var isTargetType: Int? = null
    var isCircularType: String? = null
    var isIframe = ""
    var isFileSize = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rytSend.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = resources.getString(R.string.Students_List)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.rytFilter.visibility = View.GONE
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        Constant.hideLoading(this)
        val isSelectedId = intent.getStringArrayListExtra(Constant.isSelectedId) ?: arrayListOf()
        isAcademicYearId = intent.getIntExtra(Constant.isAcademicYearId, -1)
        isCurrentAcademicYear = intent.getBooleanExtra(Constant.isCurrentAcademicYear, false)
        isAcademicYear = intent.getStringExtra(Constant.lblAcademicYear)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name




        isTargetType = Constant.isStudent
        isCircularType = Constant.student

        isAccessToken = isStaffDetails!!.access_token
        isGetStudentList(isSelectedId, isAcademicYearId)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()



        appViewModel!!.isStudentList!!.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    ShowData()
                    binding.lblNoRecordsFound.visibility = View.GONE
                    isStudentList = response.data
                    isStudentData = isStudentList
                    isStudentData()
                } else {
                    binding.rcySpecificStudent.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rcySpecificStudent.visibility = View.GONE
                ErrorMessage(getString(R.string.no_student_found))
            }
        }

        binding.toolbarLayout.cbSelect.setOnClickListener {
            if (binding.toolbarLayout.cbSelect.isChecked) {
                isSpecificStudent.clear()
                isStudentData?.forEach {
                    isSpecificStudent.add(it)
                }
                mAdapter.selectAll(true)
            } else {
                isSpecificStudent.clear()
                mAdapter.selectAll(false)
            }
        }

        appViewModel!!.isAttachmentSend?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)
            }
        }
        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, this)

            }
        }

        binding.toolbarLayout.txtSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.toolbarLayout.cbSelect.visibility = View.GONE
            }
        }


        binding.toolbarLayout.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })

    }

    fun ShowData() {
        binding.rcySpecificStudent.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }


    private fun filter(query: String) {
        val searchWords = query.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isStudentList.orEmpty()
        } else {
            isStudentList.orEmpty().filter { student ->
                val fieldsToSearch = listOf(
                    student.name?.lowercase().orEmpty(),
                    student.admission_no?.lowercase().orEmpty(),
                    student.roll_no?.lowercase().orEmpty()
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        isStudentData = filteredList

        if (filteredList.isNotEmpty()) {
            binding.rcySpecificStudent.visibility = View.VISIBLE
            mAdapter.updateList(filteredList)
        } else {
            binding.rcySpecificStudent.visibility = View.GONE
            ErrorMessage(getString(R.string.no_student_found))
        }
    }



    private fun isStudentData() {
        binding.rcySpecificStudent.layoutManager = LinearLayoutManager(this)
        mAdapter =
            SpecificStudentAdapter(
                isStudentData,
                this,
                this,
                Constant.isShimmerViewDisable
            )
        binding.rcySpecificStudent.adapter = mAdapter

    }

    private fun isGetStudentList(isSelectedId: ArrayList<String>, isAcademicYearId: Int) {
        binding.rcySpecificStudent.layoutManager = LinearLayoutManager(this)
        mAdapter =
            SpecificStudentAdapter(
                null,
                this,
                this,
                Constant.isShimmerViewShow
            )
        binding.rcySpecificStudent.adapter = mAdapter


        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSelectedId[0].toString(), isAcademicYearId, this
        )
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
        if (Constant.selectedFiles.size == 0) {
            if (SELECTED_SCHOOL_MENU == Constant.M_COMMUNICATION) {
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
                                Log.d("SELECTED_SCHOOL_MENU", SELECTED_SCHOOL_MENU.toString())
                                if (SELECTED_SCHOOL_MENU == Constant.M_COMMUNICATION) {
                                    voiceSendApi()
                                } else if (SELECTED_SCHOOL_MENU == Constant.M_ATTACHMENTS) {
                                    attachmentSendApi()
                                }
//                               else if (SELECTED_SCHOOL_MENU == Constant.M_SCHOOL_CLASS_EVENTS) {
//                                    eventsendapi()
//                                }
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
            schoolId = selectedIds,
            targetType = isTargetType!!,
            circularType = isCircularType!!,
            fileName = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)

    }

    fun attachmentSendApi() {

        isTargetType = Constant.isStudent
        isCircularType = Constant.student
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

        var isTargetType: Int? = null
        var isCircularType: String? = null
        isTargetType = Constant.isStudent
        isCircularType = Constant.student
        val isTextData = Constant.isTextSendingData

        alertMessage.text = isMessage
        lblSelectTarget.text = isSelectTarget

        okButton.setOnClickListener {
            alertDialog.dismiss()
            Constant.showLoading(this@SpecificStudent)
            if (SELECTED_SCHOOL_MENU == Constant.M_ATTACHMENTS) {
                if (Constant.selectedFiles.isNotEmpty()) {
                    val videoFiles = Constant.selectedFiles.filter { it.type == FileType.VIDEO }
                    if (videoFiles.isNotEmpty()) {
                        videoUploading()
                    } else {
                        isFileUploadInAws(
                            isStaffDetails!!.school_id,
                            "audio"
                        )
                    }
                }
            } else if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                val isTextData = Constant.isTextSendingData
                if (Constant.isCommunicationType == 3) {
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
                        voiceSendApi()
                    } else {
                        isFileUploadInAws(
                            isStaffDetails!!.school_id,
                            "files"
                        )
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    private fun videoUploading() {
        VimeoVideoUpload.uploadVideo(
            this@SpecificStudent,
            "quiz",
            "quiz",
            Constant.selectedFiles[0].path,
            this@SpecificStudent
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
            if (SELECTED_SCHOOL_MENU == Constant.M_ATTACHMENTS) {
                attachmentSendApi()
            }
        }
    }

    fun extractVimeoUrlFromIframe(iframeHtml: String): String? {
        val regex = Regex("""<iframe[^>]+src="([^"]+)"""")
        val match = regex.find(iframeHtml)
        return match?.groups?.get(1)?.value
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

    override fun onResume() {
        super.onResume()
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

            R.id.rytSend -> {
                selectedIds = isSpecificStudent.map { it.id.toString() }.toMutableList()
                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {

                    var isAcademicYearNote: String? = null
                    if (!isCurrentAcademicYear) {
                        isAcademicYearNote =
                            resources.getString(R.string.NOTE_message_addressed) + isAcademicYear + resources.getString(
                                R.string.which_communication_academic
                            )
                    } else {
                        isAcademicYearNote =
                            resources.getString(R.string.are_you_sure_want_to_send_this_message)
                    }

                    if (Constant.isCommunicationType == 3) {
                        showSendConfirmationDialog(
                            resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + resources.getString(
                                R.string.Student_s
                            ),
                            isAcademicYearNote.toString()
                        )
                    } else {
                        showSendConfirmationDialog(
                            resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + resources.getString(
                                R.string.Student_s
                            ),
                            isAcademicYearNote.toString()
                        )
                    }
                } else {
                    Constant.showValidationAlertPopup(
                        getString(
                            R.string.alert
                        ),
                        resources.getString(R.string.Please_select_least_student),
                        this
                    )

                }
            }
        }
    }

    override fun onBackPressed() {
        val searchText = binding.toolbarLayout.txtSearch.text.toString().trim()

        if (binding.toolbarLayout.txtSearch.hasFocus()) {
            binding.toolbarLayout.txtSearch.clearFocus()

            // Hide keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)

            // Show checkbox only if search is empty
            if (searchText.isEmpty()) {
                binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
            }
        } else {
            super.onBackPressed()
        }
    }





    override fun onIdCheck(data: NameAndIds) {
        if (!isSpecificStudent.any { it.id == data.id }) {
            isSpecificStudent.add(data)
        }
//        binding.toolbarLayout.cbSelect.isChecked = isSpecificStudent.size == isStudentData?.size
        binding.toolbarLayout.cbSelect.isChecked = isSpecificStudent.size == isStudentList.size

    }

    override fun onIdUnchecked(data: NameAndIds) {
        isSpecificStudent.removeAll { it.id == data.id }
        binding.toolbarLayout.cbSelect.isChecked = false

    }
}