package com.vs.schoolmessenger.School.InteractionWithStudent


import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.InteractionWithStudent.Listener.ReplyClickListener
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequest
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequestFilePath
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.InteractionwithStudentChatscreenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InteractionWithStudentChatScreen : BaseActivity<InteractionwithStudentChatscreenBinding>(),
    ReplyClickListener, View.OnClickListener {

    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var type: Boolean = false
    private lateinit var interactionWithQuestionAdapter: InteractionWithQuestionAdapter

    val QuestionDataSending = Constant.QuestionDataSending

    private var selectedQuestionId: String? = null


    override fun getViewBinding(): InteractionwithStudentChatscreenBinding {
        return InteractionwithStudentChatscreenBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token
        fetchQuestionData()
        binding.replytext.setOnClickListener(this)
        binding.replyalltext.setOnClickListener(this)
        binding.imgCloseReply.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        appViewModel?.getstaffquestions?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }
            if (response.status) {
                isLoadChatQuestionData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }

        appViewModel!!.sendanswer?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    fetchQuestionData()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        binding.lblStudentName.text = QuestionDataSending?.name ?: ""
        binding.lblStudentSection.text = QuestionDataSending?.subject_name ?: ""
    }


    private fun fetchQuestionData() {
        appViewModel?.getstaffquestions(
            isAccessToken ?: "",
            QuestionDataSending?.is_class_teacher ?: false,
            QuestionDataSending?.section_id ?: "",
            QuestionDataSending?.subject_id ?: "",
            0
        )
    }


    private fun isLoadChatQuestionData(data: List<QuestionData>) {
        if (data.isNullOrEmpty()) {
            showErrorUI(getString(R.string.no_staff_data_available))
            return
        }

        val inputFormat = SimpleDateFormat(Constant.dd_MM_yyyy_hh_mm_a, Locale.getDefault())

        val sortedData = data.sortedBy {
            try {
                inputFormat.parse(it.created_on)
            } catch (e: Exception) {
                Date(0)
            }
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffQuestionchatdata.visibility = View.VISIBLE
        binding.rcystaffQuestionchatdata.layoutManager = LinearLayoutManager(this)

        interactionWithQuestionAdapter =
            InteractionWithQuestionAdapter(sortedData, this, this, false)
        binding.rcystaffQuestionchatdata.adapter = interactionWithQuestionAdapter

        binding.rcystaffQuestionchatdata.scrollToPosition(sortedData.size - 1)
    }


    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffQuestionchatdata.visibility = View.GONE
    }

    private fun isMessageSend(replyType: String) {
        val question = binding.edtMessage.text.toString()
        if (question.isEmpty()) {
            binding.edtMessage.error = getString(R.string.This_field_required)
            return
        }

        if (selectedQuestionId.isNullOrEmpty()) {
            Constant.showDataValidation(getString(R.string.error),
                getString(R.string.invalid_question_id), this)
            return
        }

        val fileList = emptyList<AnswerModelRequestFilePath>()

        val request = AnswerModelRequest(
            question_id = selectedQuestionId!!,
            answer = question,
            reply_type = replyType,
            is_change_answer = type,
            file_path = fileList
        )

        appViewModel?.sendanswer(isAccessToken!!, request)
        binding.replyLinearlayout.visibility = View.GONE
        binding.btnAdd.visibility = View.GONE
        binding.edtMessage.visibility = View.GONE
        binding.edtMessage.text?.clear()
        selectedQuestionId = null
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.replytext -> {
                isMessageSend(Constant.two)
            }

            R.id.replyalltext -> {
                isMessageSend(Constant.one)
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.imgCloseReply -> {
                binding.replyLinearlayout.visibility = View.GONE
                binding.btnAdd.visibility = View.GONE
                binding.edtMessage.visibility = View.GONE
            }
        }
    }

    override fun onAnswerClick(chat: QuestionData, position: Int) {
        binding.replyLinearlayout.visibility = View.VISIBLE
        binding.txtReplyingTo.text = "${getString(R.string.Replying_To)} ${chat.student_name}"
        binding.btnAdd.visibility = View.GONE
        binding.edtMessage.visibility = View.VISIBLE
        binding.txtquestion.text = chat.question
        selectedQuestionId = chat.id
        Log.d("Selected Question ID", selectedQuestionId.toString())
    }


    override fun onUpdateAnswerClick(
        chat: QuestionData, position: Int, type: Boolean
    ) {
        binding.replyLinearlayout.visibility = View.VISIBLE
        binding.btnAdd.visibility = View.GONE
        binding.edtMessage.visibility = View.VISIBLE
        binding.txtquestion.text = chat.question
        this.type = type
        selectedQuestionId = chat.id
        Log.d("Selected Question ID", selectedQuestionId.toString())
    }


}