package com.vs.schoolmessenger.CommonScreens.SpecificStudentData

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SpecificStudentBinding

class SpecificStudent : BaseActivity<SpecificStudentBinding>(), SpecificStudentSelectClickListener,
    View.OnClickListener {

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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = "Specific Student"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.rytFilter.visibility = View.VISIBLE
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        val isSelectedId = intent.getStringArrayListExtra("isSelectedId") ?: arrayListOf()
        isAcademicYearId = intent.getIntExtra("isAcademicYearId", -1)
        isCurrentAcademicYear = intent.getBooleanExtra("isCurrentAcademicYear", false)
        isAcademicYear = intent.getStringExtra("lblAcademicYear")
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetStudentList(isSelectedId, isAcademicYearId)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        appViewModel!!.isStudentList!!.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                isStudentData = response.data
                isStudentData()
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

        appViewModel!!.isVoiceSend?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication,this)
            }
        }
        appViewModel!!.isSendText?.observe(this) { response ->
            Constant.hideLoading(this@SpecificStudent)
            if (response != null && response.status) {
                Constant.showTopAlertPopup(response.message, Constant.isCommunication,this)

            }
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
        Constant.showLoading(this@SpecificStudent)
        appViewModel!!.isGetStudentList(
            isAccessToken!!,
            isSelectedId[0].toString(), isAcademicYearId, this
        )
    }

    private fun isFileUploadInAws(
        isFilePath: String, schoolId: String, isFileType: String?
    ) {
        val isCountryId = SharedPreference.getCountryId(this)
        isAwsUploadingPreSigned!!.getPreSignedUrl(
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
        val isVoiceData = Constant.isVoiceSendingData
        var isTargetType: Int? = null
        var isCircularType: String? = null
        isTargetType = Constant.isStudent
        isCircularType = Constant.student

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
            targetType = isTargetType,
            circularType = isCircularType,
            fileName = isVoiceData.isFileName
        )
        appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)

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

            val isTextData = Constant.isTextSendingData
            if (Constant.isClickType == 3) {
                val jsonObject = ApiCallRequest.isSendText(
                    isAcademicYearId = isAcademicYearId,
                    schoolId = selectedIds,
                    message = isTextData!!.isTitle,
                    description = isTextData.isContent,
                    targetType = isTargetType
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

            R.id.btnSend -> {
                selectedIds = isSpecificStudent.map { it.id.toString() }.toMutableList()
                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {

                    var isAcademicYearNote: String? = null
                    if (!isCurrentAcademicYear) {
                        isAcademicYearNote =
                            "NOTE : This message is addressed to student in " + isAcademicYear + " which is not the communication academic year. Do you want to proceed?"
                    } else {
                        isAcademicYearNote = "Are you sure want to send this message?"
                    }

                    if (Constant.isClickType == 3) {
                        showSendConfirmationDialog(
                            "Selected target : " + selectedIds.size.toString() +" Student (S)",
                            isAcademicYearNote.toString()
                        )
                    } else {
                        showSendConfirmationDialog(
                            "Selected target : " + selectedIds.size.toString()+" Student (S)",
                            isAcademicYearNote.toString()
                        )
                    }
                } else {
                    Constant.showValidationAlertPopup(
                        "Please select at least one student to send the message.",
                        this
                    )

                }
            }
        }
    }

    override fun onIdCheck(data: NameAndIds) {
        if (!isSpecificStudent.any { it.id == data.id }) {
            isSpecificStudent.add(data)
        }
        binding.toolbarLayout.cbSelect.isChecked = isSpecificStudent.size == isStudentData?.size
    }

    override fun onIdUnchecked(data: NameAndIds) {
        isSpecificStudent.removeAll { it.id == data.id }
        binding.toolbarLayout.cbSelect.isChecked = false
    }
}