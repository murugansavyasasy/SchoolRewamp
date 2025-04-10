package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolListActivityBinding

class SchoolList : BaseActivity<SchoolListActivityBinding>(), SchoolListClickListener,
    View.OnClickListener {

    override fun getViewBinding(): SchoolListActivityBinding {
        return SchoolListActivityBinding.inflate(layoutInflater)
    }

    private val selectedSchoolIds = mutableListOf<Int>()
    var isMultipleSchool = true
    private lateinit var mAdapter: SchoolListAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isUserDetails: UserDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null



    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.lblMultipleSchool.setOnClickListener(this)
        binding.lblSingleSchool.setOnClickListener(this)
        binding.lblSend.setOnClickListener(this)

        if (Constant.isEmergencyVoiceNoticeBoard!!) {
            binding.lnrTab.visibility = View.GONE
            binding.lblSend.visibility= View.VISIBLE
        } else {
            binding.lnrTab.visibility = View.VISIBLE
            binding.lblSend.visibility= View.VISIBLE
        }

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        isUserDetails = SharedPreference.getUserDetails(this)


        appViewModel!!.isVoiceSend?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.showAlert("Info!", response.message, this)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblSingleSchool -> {
                binding.lblSend.visibility= View.GONE
                isMultipleSchool = false
                isChangeBackRound(binding.lblSingleSchool)
            }

            R.id.lblMultipleSchool -> {
                binding.lblSend.visibility= View.VISIBLE
                isMultipleSchool = true
                isChangeBackRound(binding.lblMultipleSchool)
            }

            R.id.lblSend -> {
                for (i in selectedSchoolIds.indices) {
                    Log.d("SelectedSchoolId", selectedSchoolIds[i].toString())
                }
                if (selectedSchoolIds.isNotEmpty()) {
                    showSendConfirmationDialog("Are you want send this voice to entire school?")
                } else {
                    Constant.showAlert("Alert!", "Select the school", this)
                }
            }
        }
    }

    private fun isChangeBackRound(
        lblSelectedTab: TextView
    ) {
        isLoadData()
        // Reset backgrounds and colors
        binding.lblMultipleSchool.background = null
        binding.lblSingleSchool.background = null

        lblSelectedTab.background = ContextCompat.getDrawable(this, R.drawable.white_radious)
        lblSelectedTab.setTextColor(
            ContextCompat.getColor(
                application, R.color.black
            )
        )
    }

    override fun onItemClick(data: StaffDetails) {
        val intent = Intent(this, RecipientActivity::class.java)
        SharedPreference.putStaffDetails(this, data)
        startActivity(intent)
    }

    private fun isFileUploadInAws(
        isFilePath: String,
        bucketPath: String,
        isFileExtension: String?,
        filetype: String,
    ) {
        Log.d("isFilePath____", isFilePath)
        isAwsUploadingPreSigned!!.getPreSignedUrl(
            isFilePath,
            bucketPath,
            isFileExtension!!,
            this,
            "1",
            true,
            false,
            object : UploadCallback {
                override fun onUploadSuccess(
                    response: String?,
                    isFileUploaded: String?
                ) {
                    //isVoiceSend(isFileUploaded)
                }

                override fun onUploadError(error: String?) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun showSendConfirmationDialog(isMessage: String) {
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
                    schoolId = selectedSchoolIds,
                    targetType = Constant.isSchool,
                    circularType = Constant.school
                )
                appViewModel!!.isVoiceSend(isAccessToken!!, jsonObject, this)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}