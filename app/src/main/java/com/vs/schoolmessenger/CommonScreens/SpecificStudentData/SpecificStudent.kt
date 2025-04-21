package com.vs.schoolmessenger.CommonScreens.SpecificStudentData

import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var selectedIds = mutableListOf<Int>()
    lateinit var mAdapter: SpecificStudentAdapter
    val isSpecificStudent = mutableListOf<NameAndIds>()
    var isStudentData: List<NameAndIds>? = null
    private var isStaffDetails: StaffDetails? = null

    //    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
//        binding.rlaAcademicYear.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = "Specific Student"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.cbSelect.visibility = View.VISIBLE
        binding.toolbarLayout.rytFilter.visibility = View.VISIBLE
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val loader = rootView.findViewById<View>(R.id.loader_root)

        val isSelectedId = intent.getIntegerArrayListExtra("isSelectedId") ?: arrayListOf()
        isAcademicYearId = intent.getIntExtra("isAcademicYearId", -1)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetStudentList(isSelectedId, isAcademicYearId)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

//        isGetAcademicYear()
        appViewModel!!.isStudentList!!.observe(this) { response ->
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

//        appViewModel!!.isGetAcademicList?.observe(this) { response ->
//            if (response != null && response.status) {
//                response.data.let { academicList ->
//                    val reorderedList = academicList.sortedByDescending { it.current_academic_year }
//                    isAcademicYear = reorderedList
//                    binding.lblAcademicYear.text = isAcademicYear!![0].year
//                    isAcademicYearId = isAcademicYear!![0].id
//                    isGetGroupList()
//                }
//            }
//        }


        appViewModel!!.isVoiceSend?.observe(this) { response ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val loader = rootView.findViewById<View>(R.id.loader_root)
            loader?.let { rootView.removeView(it) }
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }
        appViewModel!!.isSendText?.observe(this) { response ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val loader = rootView.findViewById<View>(R.id.loader_root)
            loader?.let { rootView.removeView(it) }
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }
    }

//    private fun isGetAcademicYear() {
//        appViewModel!!.isGetAcademicYear(
//            isAccessToken!!, this
//        )
//    }

    private fun isStudentData() {

        mAdapter = SpecificStudentAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcySpecificStudent.layoutManager = LinearLayoutManager(this)
        binding.rcySpecificStudent.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                SpecificStudentAdapter(
                    isStudentData,
                    this,
                    this,
                    Constant.isShimmerViewDisable
                )
            binding.rcySpecificStudent.adapter = mAdapter
        }
    }

    private fun isGetStudentList(isSelectedId: ArrayList<Int>, isAcademicYearId: Int) {
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
            Constant.isPickingFileExtension,
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
    fun showSendConfirmationDialog(isMessage: String) {

        var isTargetType: Int? = null
        var isCircularType: String? = null
        isTargetType = Constant.isStudent
        isCircularType = Constant.student
        val isTextData = Constant.isTextSendingData
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val loaderView = LayoutInflater.from(this).inflate(R.layout.lottie_loader, rootView, false)


        AlertDialog.Builder(this).setTitle("Send Confirmation!").setMessage(isMessage)
            .setPositiveButton("Yes") { dialog, _ ->
                rootView.addView(loaderView)
                if (Constant.isClickType == 3) {
                    val jsonObject = ApiCallRequest.isSendText(
                        isAcademicYearId = isAcademicYearId,
                        schoolId = selectedIds,
                        message = isTextData!!.isTitle,
                        description = isTextData.isContent,
                        targetType = Constant.isStudent
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

//            R.id.rlaAcademicYear -> {
//                showAcademicDropdown(
//                    binding.rlaAcademicYear, this, isAcademicYear
//                ) { selectedYear ->
//                    binding.lblAcademicYear.text = selectedYear.year
//                    Log.d(
//                        "DropdownMenu",
//                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
//                    )
//                }
//            }

            R.id.btnSend -> {
                selectedIds = isSpecificStudent.map { it.id }.toMutableList()
                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {
                    if (Constant.isClickType == 3) {
                        showSendConfirmationDialog("Are you want send this text to entire school?")
                    } else {
                        showSendConfirmationDialog("Are you want send this voice to entire school?")
                    }
                } else {
                    Constant.showAlert("Alert!", "Select atleast one student", this)
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