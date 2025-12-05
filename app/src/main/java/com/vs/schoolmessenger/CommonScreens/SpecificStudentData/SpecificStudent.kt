package com.vs.schoolmessenger.CommonScreens.SpecificStudentData

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentSendingData
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_COMMUNICATION
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SpecificStudentBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File

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

    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isTotalSelectedItem = 0

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.cbSelect.buttonTintList = null
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

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)

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
                    binding.toolbarLayout.cbSelect.visibility = View.GONE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
                    binding.rcySpecificStudent.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.toolbarLayout.cbSelect.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
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

        appViewModel!!.isAssignmentSend?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
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


        binding.toolbarLayout.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                binding.toolbarLayout.cbSelect.visibility =
                    if (!s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun ShowData() {
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
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


    fun isUploadFilesInServer(isFileType: String?) {

        ProgressDialogHelper.show(this)
//        ProgressDialogHelper.updateProgress(10)

        if (SELECTED_MENU_ID == M_ATTACHMENTS || SELECTED_MENU_ID == M_HOMEWORK ||
            SELECTED_MENU_ID == M_SCHOOL_CLASS_EVENTS || SELECTED_MENU_ID == M_ASSIGNMENT
        ) {
            Constant.selectedFiles.removeAt(0) // Remove '+' placeholder
        }
//        ProgressDialogHelper.updateProgress(50)
        isTotalSelectedItem = Constant.selectedFiles.size
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


        when {
//            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(isFileType)
//            isVideoSelectedArrayList.isNotEmpty() -> videoUploading()
            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(
                isFileType,
                totalTasks,
                { completedTasks++; updateProgress() })

            isVideoSelectedArrayList.isNotEmpty() -> videoUploading(
                totalTasks,
                { completedTasks++; updateProgress() })
        }
//        ProgressDialogHelper.updateProgress(80)
    }


    private fun isFileUploadInAws(
        isFileType: String?,
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        Constant.isAwsUploadedFiles.clear()
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
        if (Constant.selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
                if (SELECTED_MENU_ID == M_COMMUNICATION) {
                    voiceSendApi()
                } else if (SELECTED_MENU_ID == M_ATTACHMENTS) {
                    attachmentSendApi()
                } else if (SELECTED_MENU_ID == M_ASSIGNMENT) {
                    isAssignmentSend()
                }
            } else {
                videoUploading(totalTasks, onTaskComplete)
            }
        } else {
            val outputDir =
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
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
                                    Uri.parse(original.path),
                                    "r"
                                )?.statSize
                                    ?: 0
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
                    } else {
                        Log.e("Compressor", "Failed: ${original.path}")
                    }
                    onTaskComplete()
                },
                onComplete = {
                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(newSelectedFiles)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = Constant.selectedFiles.size
                    for (i in Constant.selectedFiles.indices) {
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            Constant.selectedFiles[i].path,
                            isStaffDetails!!.school_id,
                            isFileType!!,
                            this,
                            isCountryId!!,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?,
                                    isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    Constant.isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = Constant.selectedFiles[i].type.name
                                        )
                                    )
                                    onTaskComplete()

                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
                                        if (SELECTED_MENU_ID == M_COMMUNICATION) {
                                            voiceSendApi()
                                        } else if (SELECTED_MENU_ID == M_ATTACHMENTS) {
                                            attachmentSendApi()
                                        } else if (SELECTED_MENU_ID == M_ASSIGNMENT) {
                                            isAssignmentSend()
                                        }
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading(totalTasks, onTaskComplete)
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    Log.d("isUploadIssue", error.toString())
                                    onTaskComplete()
                                }
                            })
                    }

                    Log.d("Compressor", "All files compressed and uploaded.")
                }
            )
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
                        isFileUrl = fileItem.path,
                        isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }


        if (isVideoSelectedArrayList.isNotEmpty()) {
            for (video in isVideoSelectedArrayList) {

                Thread {
                    for (x in 1..10) {
                        Thread.sleep(400)
                        runOnUiThread { onTaskComplete() }
                    }
                }.start()


//                VimeoVideoUpload.uploadVideo(
//                    this,
//                    "quiz",
//                    "quiz",
//                    video.path,
//                    object : VimeoVideoUpload.UploadCompletionListener {
//
//                        override fun onUploadComplete(success: Boolean, iframe: String?, link: String?) {
//
//                            Log.e("VIDEO_DEBUG", "Callback fired")
//
//                            Constant.isAwsUploadedFiles.add(
//                                AwsUploadedFiles(
//                                    isFileUrl = link.toString(),
//                                    isFileType = Constant.VIDEO
//                                )
//                            )
//
//                            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
//                                ProgressDialogHelper.dismiss()
//
//                                when (SELECTED_MENU_ID) {
//                                    M_ATTACHMENTS -> attachmentSendApi()
//                                    M_ASSIGNMENT -> isAssignmentSend()
//                                }
//                            }
//                        }
//
//                        override fun onFailure(errorMessage: String?) {
//                            Log.e("VIDEO_DEBUG", "Upload failed: $errorMessage")
//                        }
//                    }
//                )

                VimeoVideoUpload.uploadVideo(
                    this, "quiz", "quiz", video.path, this
                )
            }
        } else {
            ProgressDialogHelper.dismiss()
            if (SELECTED_MENU_ID == M_ASSIGNMENT) {
                isAssignmentSend()
            } else if (SELECTED_MENU_ID == M_ATTACHMENTS) {
                attachmentSendApi()
            }
        }
    }

    override fun onUploadComplete(
        success: Boolean,
        iframe: String?,
        link: String?
    ) {
        runOnUiThread {
            Log.d("link", link.toString())
            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
                ProgressDialogHelper.dismiss()
                if (SELECTED_MENU_ID == M_ATTACHMENTS) {
                    attachmentSendApi()
                } else if (SELECTED_MENU_ID == M_ASSIGNMENT) {
                    isAssignmentSend()
                }
            }
        }
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }


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
        Constant.isTextSendingData

        alertMessage.text = isMessage
        lblSelectTarget.text = isSelectTarget

        okButton.setOnClickListener {
            alertDialog.dismiss()

            when (SELECTED_MENU_ID) {
                M_ATTACHMENTS -> {
                    if (Constant.selectedFiles.size != 1) {
                        isUploadFilesInServer("file")
                    }
                }

                M_ASSIGNMENT -> {
                    if (Constant.selectedFiles.size != 1) {
                        isUploadFilesInServer("file")
                    } else {
                        when (SELECTED_MENU_ID) {
                            M_ASSIGNMENT -> isAssignmentSend()
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
                                isTargetType
                            )
                            appViewModel?.isSendText(isAccessToken!!, json, this)
                        }
                    } else {
                        isUploadFilesInServer("audio")
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rytSend -> {
                selectedIds = isSpecificStudent.map { it.id.toString() }.toMutableList()
                for (id in selectedIds) {
                    Log.d("isSelectedIds", id)
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

                    showSendConfirmationDialog(
                        resources.getString(R.string.selected_target_1) + selectedIds.size.toString() + " " + resources.getString(
                            R.string.Student_s
                        ),
                        isAcademicYearNote.toString()
                    )
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

    fun isAssignmentSend() {

        val subjectId = intent.getIntExtra("subject_id", 0)
        val isAssignmentData =
            intent.getParcelableExtra<AssignmentSendingData>(Constant.assignment_data)
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
                subjectId = subjectId
            )
            Log.d("jsonObject", jsonObject.toString())
            appViewModel!!.isSendAssignment(isAccessToken!!, jsonObject, this)

        } ?: run {
            Constant.showValidationAlertPopup(
                getString(R.string.alert),
                "Assignment details is missing.",
                this
            )
        }
    }

    override fun onIdCheck(data: NameAndIds) {
        if (!isSpecificStudent.any { it.id == data.id }) {
            isSpecificStudent.add(data)
        }
        binding.toolbarLayout.cbSelect.isChecked = isSpecificStudent.size == isStudentList.size
    }

    override fun onIdUnchecked(data: NameAndIds) {
        isSpecificStudent.removeAll { it.id == data.id }
        binding.toolbarLayout.cbSelect.isChecked = false
    }
}