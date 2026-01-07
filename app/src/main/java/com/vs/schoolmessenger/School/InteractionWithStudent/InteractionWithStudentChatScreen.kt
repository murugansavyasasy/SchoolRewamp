package com.vs.schoolmessenger.School.InteractionWithStudent


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
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
        isToolBarPrimaryInteractionwithStaff(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
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
                showErrorUI(response.message ?: getString(R.string.no_data_available))
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
        appViewModel?.isblockstudent?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    showDataValidation(
                        resources.getString(R.string.success),
                        response.message ?: getString(R.string.updated_successfully), this
                    )
                    fetchQuestionData()
                } else {
                    showDataValidation(
                        resources.getString(R.string.fail),
                        response.message, this
                    )
                }
            }
        }
        binding.lblStudentName.text =
            "${QuestionDataSending?.name ?: ""} (${QuestionDataSending?.section_name ?: ""})"
        binding.lblStudentSection.text = QuestionDataSending?.subject_name ?: ""

            enableEdgeToEdge()


    }

    private fun enableEdgeToEdge() {
        // Works on all API levels
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setWindowInsetsAnimationCallback(binding.root, null)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )

            // Status bar height
            binding.statusBarBackground.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            }

            // Bottom inset (nav bar + keyboard)
            binding.rytHeader.updatePadding(bottom = insets.bottom)

            // Scroll chat to bottom on keyboard open
            if (insets.bottom > 0 && ::interactionWithQuestionAdapter.isInitialized) {
                binding.rcystaffQuestionchatdata.post {
                    val adapter = binding.rcystaffQuestionchatdata.adapter as? InteractionWithQuestionAdapter
                    adapter?.let {
                        binding.rcystaffQuestionchatdata.scrollToPosition(it.itemCount - 1)
                    }
                }
            }

            WindowInsetsCompat.CONSUMED
        }
    }


    private fun showDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)
        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }
        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()
        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }
        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)
        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }
        okButton.setOnClickListener {
            val intent = Intent(activity, InteractionWithStudentChatScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }

    private fun fetchQuestionData() {
        appViewModel?.getstaffquestions(
            isAccessToken ?: "",
            QuestionDataSending?.is_class_teacher ?: false,
            QuestionDataSending?.section_id ?: "",
            QuestionDataSending?.subject_id ?: "",
            0,this
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
            Constant.showDataValidation(
                getString(R.string.error),
                getString(R.string.invalid_question_id), this
            )
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
        appViewModel?.sendanswer(isAccessToken!!, request,this)
        binding.replyLinearlayout.visibility = View.GONE
        binding.btnAdd.visibility = View.GONE
        binding.edtMessage.visibility = View.GONE
        binding.edtMessage.text?.clear()
        selectedQuestionId = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.replytext -> {

                // Hide keyboard if open
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)

                // Optionally clear focus (prevents reopening)
                binding.edtMessage.clearFocus()

                isMessageSend(Constant.two)
            }

            R.id.replyalltext -> {
                // Hide keyboard if open
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)

                // Optionally clear focus (prevents reopening)
                binding.edtMessage.clearFocus()
                isMessageSend(Constant.one)
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.imgCloseReply -> {
                binding.replyLinearlayout.visibility = View.GONE
                binding.btnAdd.visibility = View.GONE
                binding.edtMessage.text.clear()
                binding.edtMessage.visibility = View.GONE
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
            }
        }
    }

    override fun onAnswerClick(chat: QuestionData, position: Int) {
        binding.replyLinearlayout.visibility = View.VISIBLE
        binding.txtReplyingTo.text = "${getString(R.string.Replying_To)} ${chat.student_name}"
        Log.d("Student Name Reply Value", chat.student_name.toString())
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
        binding.txtReplyingTo.text = "${getString(R.string.Replying_To)} ${chat.student_name}"
        Log.d("Student Name Reply Value", chat.student_name.toString())
        binding.btnAdd.visibility = View.GONE
        binding.edtMessage.visibility = View.VISIBLE
        binding.txtquestion.text = chat.question
        this.type = type
        selectedQuestionId = chat.id
        Log.d("Selected Question ID", selectedQuestionId.toString())
    }

    override fun onBlockStudent(chat: QuestionData, reason: String) {
        if (chat.student_id.isNullOrEmpty()) {
            Constant.showDataValidation(
                getString(R.string.error),
                getString(R.string.invalid_student_id),
                this
            )
            return
        }
        val jsonObject = JsonObject().apply {
            addProperty("student_id", chat.student_id)
            addProperty("is_block", !chat.is_blocked)
            addProperty("reason", reason)
        }
        appViewModel?.isblockstudent(isAccessToken!!, jsonObject,this)
    }
}