package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffChatAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.FilePath
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StaffchatScreenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override fun setupViews() {
        super.setupViews()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        isToolBarPrimaryParentInteractionwithStaff(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.btnSend.setOnClickListener(this)
        isChildDetails = SharedPreference.getChildDetails(this)

        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        fetchChatData()


        appViewModel?.getstaffanswers?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }

            val mobileNumber = SharedPreference.getMobileNumber(this)
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.mobile_number, mobileNumber)
                addProperty(APIKeyNames.activity, Constant.add_points_view_chat_messages)
                addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
            }
            appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

            if (response.status) {
                isLoadChatData(response.data)
            } else {
                showErrorUI(response.message ?: getString(R.string.no_data_available))
            }
        }

        appViewModel!!.sendquestion?.observe(this) { response ->
            binding.btnSend.isEnabled = true
            if (response != null) {
                if (response.status) {
                    binding.edtMessage.text.clear()
                    fetchChatData()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.toolbarLayout.lblStudentName.text = staffData?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text = staffData?.subject_name ?: ""

        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.rcystaffchatdata.post {
                    if (::interactionWithStaffChatAdapter.isInitialized && interactionWithStaffChatAdapter.itemCount > 0) {
                        binding.rcystaffchatdata.scrollToPosition(interactionWithStaffChatAdapter.itemCount - 1)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        if (staffData!!.is_blocked == true) {
            binding.bottomChatInput.visibility = View.GONE
            binding.blockdetails.visibility = View.VISIBLE
            binding.reasontext.text = "${getString(R.string.reason)} : ${staffData!!.reason}"
        } else {
            binding.bottomChatInput.visibility = View.VISIBLE
            binding.blockdetails.visibility = View.GONE
        }

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
            showErrorUI(getString(R.string.no_staff_data_available))
            return
        }

        val inputFormat = SimpleDateFormat(Constant.dd_MM_yyyy_hh_mm_a, Locale.ENGLISH)

        val sortedData = data.sortedWith(compareBy<AnswerData> {
            try {
                inputFormat.parse(it.asked_on)
            } catch (e: Exception) {
                Date(0)
            }
        }.thenBy {
            try {
                it.question_id.toInt()
            } catch (e: Exception) {
                0
            }
        })

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffchatdata.visibility = View.VISIBLE
        binding.rcystaffchatdata.layoutManager = LinearLayoutManager(this)

        interactionWithStaffChatAdapter = InteractionWithStaffChatAdapter(sortedData, this, false)
        binding.rcystaffchatdata.adapter = interactionWithStaffChatAdapter

        binding.rcystaffchatdata.scrollToPosition(sortedData.size - 1)
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

        binding.btnSend.isEnabled = false

        val fileList = emptyList<FilePath>()

        val request = QuestionModelRequest(
            staff_id = staffData?.id ?: "",
            subject_id = staffData?.subject_id ?: "",
            question = question,
            is_class_teacher = staffData?.is_class_teacher ?: false,
            file_path = fileList
        )

        appViewModel?.sendquestion(isAccessToken!!, request, this)

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

