package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffChatAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.FilePath
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StaffchatScreenBinding

class InteractionwithStaffChatScreen : BaseActivity<StaffchatScreenBinding>(),
    View.OnClickListener {

    private var isChildDetails: ChildDetails? = null

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private lateinit var interactionWithStaffChatAdapter: InteractionWithStaffChatAdapter

    val staffData = Constant.StaffDataSending


    override fun getViewBinding(): StaffchatScreenBinding {
        return StaffchatScreenBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        binding.imgBack.setOnClickListener(this)

        binding.btnSend.setOnClickListener(this)
        isChildDetails = SharedPreference.getChildDetails(this)

        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        fetchChatData()


        appViewModel?.getstaffanswers?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadChatData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        appViewModel!!.sendquestion?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    fetchChatData()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.lblStudentName.text = staffData?.name ?: ""
        binding.lblStudentSection.text = staffData?.subject_name ?: ""
    }


    private fun fetchChatData() {
        appViewModel?.getstaffanswers(
            isAccessToken ?: "",
            staffData?.id ?: "",
            staffData?.subject_id ?: "",
            0,
            staffData?.is_class_teacher ?: false,
            this
        )
    }


    private fun isLoadChatData(data: List<AnswerData>) {

        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffchatdata.visibility = View.VISIBLE
        binding.rcystaffchatdata.layoutManager = LinearLayoutManager(this)

        interactionWithStaffChatAdapter =
            InteractionWithStaffChatAdapter(data ?: listOf(), this, false)
        binding.rcystaffchatdata.adapter = interactionWithStaffChatAdapter

    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffchatdata.visibility = View.GONE
    }

    private fun isMessageSend() {

        var question = binding.edtMessage.text.toString()


        if (question.isEmpty()) {
            binding.edtMessage.error = getString(R.string.This_field_required)
            return
        }

        val fileList = emptyList<FilePath>()

        val request = QuestionModelRequest(
            staff_id = staffData?.id ?: "",
            subject_id = staffData?.subject_id ?: "",
            question = question,
            is_class_teacher = staffData?.is_class_teacher ?: false,
            file_path = fileList
        )

        appViewModel?.sendquestion(isAccessToken!!, request)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                isMessageSend()
            }

            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}

