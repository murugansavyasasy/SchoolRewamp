package com.vs.schoolmessenger.CommonScreens.SpecificStudentData

import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
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

        val isSelectedId = intent.getIntegerArrayListExtra("isSelectedId") ?: arrayListOf()
        val isAcademicYearId = intent.getIntExtra("isAcademicYearId", -1)
        val isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetStudentList(isSelectedId, isAcademicYearId)

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


        appViewModel!!.isVoiceSend?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
            }
        }
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(isMessage: String) {

        var isTargetType: Int? = null
        var isCircularType: String? = null
        isTargetType = Constant.isStudent
        isCircularType = Constant.student

        val isVoiceData = Constant.isVoiceSendingData
        AlertDialog.Builder(this).setTitle("Send Confirmation!").setMessage(isMessage)
            .setPositiveButton("Yes") { dialog, _ ->
                val isVoiceUrl =
                    "https://schoolchimes-communication.s3.ap-south-1.amazonaws.com/2025-04-09/5512/audiorecord.m4a"
                val jsonObject = ApiCallRequest.isVoiceSend(
                    isFileUploaded = isVoiceUrl,
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
                    fileName = "sss_12-04-2025.mp3"
                )
                appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
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

            R.id.btnSend -> {
                selectedIds = isSpecificStudent.map { it.id }.toMutableList()
                for (id in selectedIds) {
                    Log.d("isSelectedIds", id.toString())
                }
                if (selectedIds.isNotEmpty()) {
                    showSendConfirmationDialog("Are you want send this voice?")
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